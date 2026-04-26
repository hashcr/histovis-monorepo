package org.histovis.analysisservice.dto;

import org.histovis.analysisservice.common.JobStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record JobDto(
    UUID id,
    String pluginCode,
    UUID imageId,
    String imageUrl,
    Map<String, String> args,
    JobStatus status,
    LocalDateTime date,
    LocalDateTime completedDate,
    String output,
    String username
) {}
