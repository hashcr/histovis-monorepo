package org.histovis.analysisservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record SubmitJobRequest(
    @NotBlank(message = "pluginCode is required")
    String pluginCode,

    @NotNull(message = "imageId is required")
    UUID imageId,

    @NotBlank(message = "imageUrl is required")
    String imageUrl,

    Map<String, String> args
) {
    public SubmitJobRequest {
        if (args == null) args = Map.of();
    }
}
