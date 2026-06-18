package org.histovis.analysisservice.dto;

import java.util.UUID;

public record VerifyPluginMessage(
        UUID pluginId,
        String pluginCode,
        String scriptUrl,
        String installationTopicRoute
) {}
