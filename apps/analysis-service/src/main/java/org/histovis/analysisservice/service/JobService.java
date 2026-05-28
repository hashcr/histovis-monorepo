package org.histovis.analysisservice.service;

import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.config.RabbitMQConfig;
import org.histovis.analysisservice.dto.JobDto;
import org.histovis.analysisservice.dto.JobMessage;
import org.histovis.analysisservice.dto.PyramidalImageSetupMessage;
import org.histovis.analysisservice.dto.SubmitJobRequest;
import org.histovis.analysisservice.dto.response.GetJobResponse;
import org.histovis.analysisservice.dto.response.ListJobsResponse;
import org.histovis.analysisservice.dto.response.SubmitJobResponse;
import org.histovis.analysisservice.common.JobStatus;
import org.histovis.analysisservice.exception.JobNotFoundException;
import org.histovis.analysisservice.mapper.JobMapper;
import org.histovis.analysisservice.model.Job;
import org.histovis.analysisservice.model.Plugin;
import org.histovis.analysisservice.repository.JobRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final PluginService pluginService;
    private final RabbitTemplate rabbitTemplate;

    public JobService(JobRepository jobRepository, JobMapper jobMapper,
                      PluginService pluginService, RabbitTemplate rabbitTemplate) {
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
        this.pluginService = pluginService;
        this.rabbitTemplate = rabbitTemplate;
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

        Plugin plugin = pluginService.findByCode(request.pluginCode());
        JobMessage message = new JobMessage(saved.getId(), saved.getImageUrl(), saved.getArgs());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, plugin.getTopic(), message);
        log.info("Job dispatched to exchange={} topic={} jobId={}", RabbitMQConfig.EXCHANGE, plugin.getTopic(), saved.getId());

        return new SubmitJobResponse(saved.getId());
    }

    public void publishPyramidalImageSetup(UUID imageId, String imageUrl) {
        PyramidalImageSetupMessage message = new PyramidalImageSetupMessage(imageId, imageUrl);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TILESERVER_SETUP_WSI_ROUTING_KEY, message);
        log.info("PyramidalImageSetup dispatched exchange={} topic={} imageId={}",
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.TILESERVER_SETUP_WSI_ROUTING_KEY, imageId);
    }

    @Transactional
    public void updateJobResult(UUID id, JobStatus status, String output) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        job.setStatus(status);
        job.setOutput(output);
        if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
            job.setCompletedDate(LocalDateTime.now());
        }
        log.info("Job updated id={} status={}", id, status);
    }
}
