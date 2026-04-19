package org.histovis.analysisservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.dto.response.ListPluginsResponse;
import org.histovis.analysisservice.utils.Constants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(Constants.ANALYSIS_BASE_URL + Constants.PLUGINS_BASE_URL)
public class PluginController {

    @GetMapping
    public ListPluginsResponse list() {
        return new ListPluginsResponse(List.of());
    }
}
