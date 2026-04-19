package org.histovis.analysisservice.mapper;

import org.histovis.analysisservice.dto.PluginDto;
import org.histovis.analysisservice.model.Plugin;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PluginMapper {

    public PluginDto toDto(Plugin plugin) {
        return new PluginDto(
                plugin.getId(),
                plugin.getCode(),
                plugin.getName(),
                plugin.getDescription(),
                plugin.getQueue(),
                plugin.getTopic(),
                plugin.getExampleArgs(),
                plugin.getInstalledBy(),
                plugin.getInstalledDate(),
                plugin.getReadme()
        );
    }

    public List<PluginDto> toDtoList(List<Plugin> plugins) {
        return plugins.stream().map(this::toDto).toList();
    }
}
