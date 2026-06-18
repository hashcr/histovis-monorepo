package org.histovis.analysisservice.dto;

import org.histovis.analysisservice.common.PluginStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record PluginDto(
    UUID id,
    String code,
    String name,
    String description,
    String queue,
    String topic,
    Map<String, String> exampleArgs,
    String installedBy,
    LocalDateTime installedDate,
    String readme,
    String scriptUrl,
    PluginStatus status
) {}
