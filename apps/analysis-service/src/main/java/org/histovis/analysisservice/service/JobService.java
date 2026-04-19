package org.histovis.analysisservice.service;

import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.dto.JobDto;
import org.histovis.analysisservice.dto.SubmitJobRequest;
import org.histovis.analysisservice.dto.response.GetJobResponse;
import org.histovis.analysisservice.dto.response.ListJobsResponse;
import org.histovis.analysisservice.dto.response.SubmitJobResponse;
import org.histovis.analysisservice.exception.JobNotFoundException;
import org.histovis.analysisservice.mapper.JobMapper;
import org.histovis.analysisservice.model.Job;
import org.histovis.analysisservice.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public JobService(JobRepository jobRepository, JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
    }

    @Transactional(readOnly = true)
    public GetJobResponse getJob(UUID id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        return new GetJobResponse(jobMapper.toDto(job));
    }

    @Transactional(readOnly = true)
    public ListJobsResponse listByImage(UUID imageId) {
        List<JobDto> jobs = jobMapper.toDtoList(jobRepository.findByImageId(imageId));
        return new ListJobsResponse(jobs);
    }

    @Transactional
    public SubmitJobResponse submit(SubmitJobRequest request, String username) {
        Job job = jobMapper.fromRequest(request, username);
        Job saved = jobRepository.save(job);
        log.info("Job submitted id={} plugin={} by user={}", saved.getId(), saved.getPluginCode(), username);
        return new SubmitJobResponse(saved.getId());
    }
}
