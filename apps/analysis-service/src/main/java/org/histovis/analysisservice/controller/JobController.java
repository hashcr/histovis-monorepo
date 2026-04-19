package org.histovis.analysisservice.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.dto.SubmitJobRequest;
import org.histovis.analysisservice.dto.response.GetJobResponse;
import org.histovis.analysisservice.dto.response.ListJobsResponse;
import org.histovis.analysisservice.dto.response.SubmitJobResponse;
import org.histovis.analysisservice.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(Constants.ANALYSIS_BASE_URL + Constants.JOBS_BASE_URL)
public class JobController {

    @GetMapping("/{id}")
    public GetJobResponse get(@PathVariable UUID id) {
        return new GetJobResponse(null);
    }

    @GetMapping
    public ListJobsResponse list(@RequestParam String imageUrl) {
        return new ListJobsResponse(List.of());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmitJobResponse submit(@RequestBody @Valid SubmitJobRequest request) {
        return new SubmitJobResponse(UUID.randomUUID());
    }
}
