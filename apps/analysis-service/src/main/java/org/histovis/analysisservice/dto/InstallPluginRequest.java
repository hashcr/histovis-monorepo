package org.histovis.analysisservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record InstallPluginRequest(
    @NotBlank(message = "code is required")
    String code,

    @NotBlank(message = "name is required")
    String name,

    String description,

    @NotBlank(message = "queue is required")
    String queue,

    @NotBlank(message = "topic is required")
    String topic,

    Map<String, String> exampleArgs,

    String readme
) {
    public InstallPluginRequest {
        if (exampleArgs == null) exampleArgs = Map.of();
    }
}
