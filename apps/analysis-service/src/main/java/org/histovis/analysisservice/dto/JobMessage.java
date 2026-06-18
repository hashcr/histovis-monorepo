package org.histovis.analysisservice.dto;

import java.util.Map;
import java.util.UUID;

public record JobMessage(
        UUID jobId,
        UUID imageId,
        String imageUrl,
        Map<String, String> args
) {}
