package org.histovis.analysisservice.dto;

import java.util.Map;
import java.util.UUID;

public record JobMessage(
        UUID jobId,
        String imageUrl,
        Map<String, String> args
) {}
