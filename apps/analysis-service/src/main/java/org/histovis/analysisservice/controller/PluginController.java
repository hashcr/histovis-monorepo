package org.histovis.analysisservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.dto.InstallPluginRequest;
import org.histovis.analysisservice.dto.PluginDto;
import org.histovis.analysisservice.dto.response.ListPluginsResponse;
import org.histovis.analysisservice.service.PluginService;
import org.histovis.analysisservice.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(Constants.ANALYSIS_BASE_URL + Constants.PLUGINS_BASE_URL)
public class PluginController {

    private final PluginService pluginService;
    private final ObjectMapper objectMapper;

    public PluginController(PluginService pluginService, ObjectMapper objectMapper) {
        this.pluginService = pluginService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ListPluginsResponse list() {
        return new ListPluginsResponse(this.pluginService.listAll());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PluginDto install(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String queue,
            @RequestParam String topic,
            @RequestParam(required = false) String exampleArgs,
            @RequestParam(required = false) String readme,
            @RequestParam MultipartFile scriptFile,
            Authentication authentication) {

        log.info("Install plugin request: code={}, user={}", code, authentication.getName());
        InstallPluginRequest request = new InstallPluginRequest(code, name, description, queue, topic, parseExampleArgs(exampleArgs), readme);
        return pluginService.installPlugin(request, scriptFile, authentication.getName());
    }

    private Map<String, String> parseExampleArgs(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("Could not parse exampleArgs JSON: {}", json);
            return Map.of();
        }
    }
}
