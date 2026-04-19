package org.histovis.analysisservice.dto;

import jakarta.validation.constraints.NotNull;
import org.histovis.analysisservice.common.JobStatus;

public record UpdateJobResultRequest(
        @NotNull(message = "status is required") JobStatus status,
        String output
) {}
