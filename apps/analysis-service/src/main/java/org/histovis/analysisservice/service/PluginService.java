package org.histovis.analysisservice.service;


import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.dto.PluginDto;
import org.histovis.analysisservice.mapper.PluginMapper;
import org.histovis.analysisservice.repository.PluginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class PluginService {

    private final PluginRepository pluginRepository;
    private final PluginMapper pluginMapper;

    public PluginService(PluginRepository pluginRepository, PluginMapper pluginMapper) {
        this.pluginRepository = pluginRepository;
        this.pluginMapper = pluginMapper;
    }

    @Transactional(readOnly = true)
    public List<PluginDto> listAll() {
        return this.pluginMapper.toDtoList(this.pluginRepository.findAll());
    }
}
