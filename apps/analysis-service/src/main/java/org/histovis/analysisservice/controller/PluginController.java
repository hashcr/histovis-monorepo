package org.histovis.analysisservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.dto.response.ListPluginsResponse;
import org.histovis.analysisservice.service.PluginService;
import org.histovis.analysisservice.utils.Constants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(Constants.ANALYSIS_BASE_URL + Constants.PLUGINS_BASE_URL)
public class PluginController {

    private final PluginService pluginService;

    public PluginController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @GetMapping
    public ListPluginsResponse list() {
        return new ListPluginsResponse(this.pluginService.listAll());
    }
}
