package org.histovis.analysisservice.mapper;

import org.histovis.analysisservice.common.JobStatus;
import org.histovis.analysisservice.dto.JobDto;
import org.histovis.analysisservice.dto.SubmitJobRequest;
import org.histovis.analysisservice.model.Job;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class JobMapper {

    public JobDto toDto(Job job) {
        return new JobDto(
                job.getId(),
                job.getPluginCode(),
                job.getImageId(),
                job.getImageUrl(),
                job.getArgs(),
                job.getStatus(),
                job.getDate(),
                job.getCompletedDate(),
                job.getOutput(),
                job.getUsername()
        );
    }

    public List<JobDto> toDtoList(List<Job> jobs) {
        return jobs.stream().map(this::toDto).toList();
    }

    public Job fromRequest(SubmitJobRequest request, String username) {
        Job job = new Job();
        job.setPluginCode(request.pluginCode());
        job.setImageId(request.imageId());
        job.setImageUrl(request.imageUrl());
        job.setArgs(request.args());
        job.setStatus(JobStatus.PENDING);
        job.setDate(LocalDateTime.now());
        job.setUsername(username);
        return job;
    }
}
