package org.histovis.analysisservice.dto.response;

import org.histovis.analysisservice.dto.JobDto;

import java.util.List;

public record ListJobsResponse(
    List<JobDto> jobs
) {}
