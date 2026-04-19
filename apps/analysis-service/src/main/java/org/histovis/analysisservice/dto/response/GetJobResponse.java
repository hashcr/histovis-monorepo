package org.histovis.analysisservice.dto.response;

import org.histovis.analysisservice.dto.JobDto;

public record GetJobResponse(
    JobDto job
) {}
