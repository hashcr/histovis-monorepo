package org.histovis.analysisservice.dto.response;

import org.histovis.analysisservice.dto.PluginDto;

import java.util.List;

public record ListPluginsResponse(
    List<PluginDto> plugins
) {}
