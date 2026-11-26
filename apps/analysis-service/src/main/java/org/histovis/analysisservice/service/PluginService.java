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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
public class PluginService {

    private static final Pattern VALID_CODE_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final Set<String> RESERVED_CODES = Set.of("install", "verify_plugin");

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
        validateInstallRequest(request);

        return pluginRepository.findByCode(request.code())
                .map(existing -> {
                    if (existing.getStatus() == PluginStatus.FAILED) {
                        log.info("Plugin '{}' previously failed; retrying installation.", request.code());
                        return populateAndQueue(existing, request, scriptFile, installedBy);
                    }
                    log.info("Plugin '{}' already exists with status {}, returning existing.", request.code(), existing.getStatus());
                    return pluginMapper.toDto(existing);
                })
                .orElseGet(() -> populateAndQueue(new Plugin(), request, scriptFile, installedBy));
    }

    private void validateInstallRequest(InstallPluginRequest request) {
        String code = request.code();
        if (!VALID_CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Invalid plugin code '" + code + "'. Must be a valid Python identifier (letters, digits, underscores, not starting with a digit).");
        }
        if (RESERVED_CODES.contains(code)) {
            throw new IllegalArgumentException("Plugin code '" + code + "' is reserved.");
        }
        String expectedQwenTopic = "job.qwen." + code;
        String expectedStardistTopic = "job.stardist." + code;
        if (!request.topic().equals(expectedQwenTopic) && !request.topic().equals(expectedStardistTopic)) {
            throw new IllegalArgumentException(
                    "Invalid topic '" + request.topic() + "'. Expected '" + expectedQwenTopic + "' or '" + expectedStardistTopic + "'.");
        }
    }

    private PluginDto populateAndQueue(Plugin plugin, InstallPluginRequest request, MultipartFile scriptFile, String installedBy) {
        String scriptUrl = uploadScript(request.code(), scriptFile);
        plugin.setCode(request.code());
        plugin.setName(request.name());
        plugin.setDescription(request.description());
        plugin.setQueue(request.queue());
        plugin.setTopic(request.topic());
        plugin.setExampleArgs(new HashMap<>(request.exampleArgs()));
        plugin.setReadme(request.readme());
        plugin.setScriptUrl(scriptUrl);
        plugin.setInstalledBy(installedBy);
        plugin.setInstalledDate(LocalDateTime.now());
        plugin.setStatus(PluginStatus.PENDING);
        Plugin saved = pluginRepository.save(plugin);
        VerifyPluginMessage message = new VerifyPluginMessage(
                saved.getId(), saved.getCode(), saved.getScriptUrl(), saved.getTopic());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.VERIFY_PLUGIN_ROUTING_KEY, message);
        log.info("Plugin '{}' queued for verification.", saved.getCode());
        return pluginMapper.toDto(saved);
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
