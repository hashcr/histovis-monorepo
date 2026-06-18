package org.histovis.analysisservice.service;

import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.common.PluginStatus;
import org.histovis.analysisservice.config.RabbitMQConfig;
import org.histovis.analysisservice.dto.InstallPluginRequest;
import org.histovis.analysisservice.dto.PluginDto;
import org.histovis.analysisservice.dto.VerifyPluginMessage;
import org.histovis.analysisservice.exception.PluginNotFoundException;
import org.histovis.analysisservice.mapper.PluginMapper;
import org.histovis.analysisservice.model.Plugin;
import org.histovis.analysisservice.repository.PluginRepository;
import org.histovis.commons.storage.ObjectStorageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PluginService {

    private final PluginRepository pluginRepository;
    private final PluginMapper pluginMapper;
    private final ObjectStorageService storageService;
    private final RabbitTemplate rabbitTemplate;

    public PluginService(PluginRepository pluginRepository, PluginMapper pluginMapper,
                         ObjectStorageService storageService, RabbitTemplate rabbitTemplate) {
        this.pluginRepository = pluginRepository;
        this.pluginMapper = pluginMapper;
        this.storageService = storageService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(readOnly = true)
    public List<PluginDto> listAll() {
        return this.pluginMapper.toDtoList(this.pluginRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Plugin findByCode(String code) {
        return pluginRepository.findByCode(code)
                .orElseThrow(() -> new PluginNotFoundException(code));
    }

    public PluginDto installPlugin(InstallPluginRequest request, MultipartFile scriptFile, String installedBy) {
        return pluginRepository.findByCode(request.code())
                .map(existing -> {
                    log.info("Plugin '{}' already exists, returning existing.", request.code());
                    return pluginMapper.toDto(existing);
                })
                .orElseGet(() -> {
                    String scriptUrl = uploadScript(request.code(), scriptFile);
                    Plugin plugin = new Plugin();
                    plugin.setCode(request.code());
                    plugin.setName(request.name());
                    plugin.setDescription(request.description());
                    plugin.setQueue(request.queue());
                    plugin.setTopic(request.topic());
                    plugin.setExampleArgs(request.exampleArgs());
                    plugin.setReadme(request.readme());
                    plugin.setScriptUrl(scriptUrl);
                    plugin.setInstalledBy(installedBy);
                    plugin.setInstalledDate(LocalDateTime.now());
                    plugin.setStatus(PluginStatus.PENDING);
                    Plugin saved = pluginRepository.save(plugin);
                    VerifyPluginMessage message = new VerifyPluginMessage(
                            saved.getId(), saved.getCode(), saved.getScriptUrl(), saved.getTopic());
                    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.VERIFY_PLUGIN_ROUTING_KEY, message);
                    pluginRepository.save(saved);
                    log.info("Plugin '{}' queued for verification.", saved.getCode());
                    return pluginMapper.toDto(saved);
                });
    }

    public void updateStatus(UUID id, PluginStatus status) {
        Plugin plugin = pluginRepository.findById(id)
                .orElseThrow(() -> new PluginNotFoundException(id.toString()));
        plugin.setStatus(status);
        pluginRepository.save(plugin);
        log.info("Plugin '{}' status updated to {}.", plugin.getCode(), status);
    }

    private String uploadScript(String code, MultipartFile scriptFile) {
        String key = "plugins/" + code + "/" + code + ".py";
        try {
            return storageService.upload(key, scriptFile.getInputStream(), scriptFile.getSize(), "text/x-python");
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read script file for plugin: " + code, e);
        }
    }
}
