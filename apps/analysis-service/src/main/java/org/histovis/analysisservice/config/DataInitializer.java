package org.histovis.analysisservice.config;

import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.model.Plugin;
import org.histovis.analysisservice.repository.PluginRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final PluginRepository pluginRepository;

    public DataInitializer(PluginRepository pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    @Override
    public void run(String... args) {
        if (pluginRepository.findByCode("describe_wsi").isEmpty()) {
            Plugin plugin = new Plugin();
            plugin.setCode("describe_wsi");
            plugin.setName("Describe Whole Slide Image");
            plugin.setDescription("Uses a large language model to generate a structured natural language description of a whole slide histopathology image, summarizing tissue morphology, staining patterns, and notable regions of interest.");
            plugin.setQueue("analysis.exchange");
            plugin.setTopic("job.qwen.describe_wsi");
            plugin.setExampleArgs(Map.of());
            plugin.setInstalledBy("system");
            plugin.setInstalledDate(LocalDateTime.now());
            plugin.setReadme("""
                    # Describe WSI Plugin

                    ## Overview
                    This plugin analyzes a whole slide image (WSI) using a large language model (LLaMA)
                    to produce a structured natural language description of the slide.

                    ## What it does
                    - Identifies tissue type and staining (e.g. H&E, IHC)
                    - Describes overall tissue architecture and morphology
                    - Highlights notable regions such as tumor nests, necrosis, or inflammatory infiltrates
                    - Produces a concise pathology-style summary

                    ## Arguments
                    This plugin requires no additional arguments.

                    ## Output
                    A plain text pathology description of the slide.

                    ## Notes
                    - Processing time depends on image size and model load
                    - Results are non-diagnostic and intended for research use only
                    """);
            pluginRepository.save(plugin);
            log.info("Default plugin 'describe_wsi' created.");
        }
    }
}
