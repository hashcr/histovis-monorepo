package org.histovis.analysisservice.dto;

import jakarta.validation.constraints.NotNull;
import org.histovis.analysisservice.common.PluginStatus;

public record UpdatePluginStatusRequest(
        @NotNull(message = "status is required") PluginStatus status
) {}
