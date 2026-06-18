package org.histovis.analysisservice.config;

import lombok.extern.slf4j.Slf4j;
import org.histovis.analysisservice.common.PluginStatus;
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
            plugin.setStatus(PluginStatus.INSTALLED);
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

        if (pluginRepository.findByCode("he_nuclei_count").isEmpty()) {
            Plugin plugin = new Plugin();
            plugin.setCode("he_nuclei_count");
            plugin.setName("HE Nuclei Count");
            plugin.setDescription("Detects and counts nuclei in H&E stained histopathology images, providing a total nuclei count and density map.");
            plugin.setQueue("analysis.exchange");
            plugin.setTopic("job.stardist.he_nuclei_count");
            plugin.setExampleArgs(Map.of("prob_thresh", "0.5", "nms_thresh", "0.4"));
            plugin.setInstalledBy("system");
            plugin.setInstalledDate(LocalDateTime.now());
            plugin.setStatus(PluginStatus.INSTALLED);
            plugin.setReadme("""
                    # HE Nuclei Count Plugin

                    ## Overview
                    This plugin detects and counts nuclei in H&E stained histopathology images.

                    ## What it does
                    - Identifies individual nuclei in H&E stained tissue sections
                    - Returns a total nuclei count for the image
                    - Produces a density map highlighting nucleus locations

                    ## Arguments
                    - `prob_thresh` — probability threshold for nucleus detection (default: 0.5)
                    - `nms_thresh` — non-maximum suppression threshold (default: 0.4)

                    ## Output
                    A nuclei count and spatial density map of detected nuclei.

                    ## Notes
                    - Optimised for H&E stained sections
                    - Results are non-diagnostic and intended for research use only
                    """);
            pluginRepository.save(plugin);
            log.info("Default plugin 'he_nuclei_count' created.");
        }

        if (pluginRepository.findByCode("ihc_positive_cells").isEmpty()) {
            Plugin plugin = new Plugin();
            plugin.setCode("ihc_positive_cells");
            plugin.setName("IHC Positive Cell Quantification");
            plugin.setDescription("Quantifies DAB-positive cells in IHC-stained histopathology images, classifying each nucleus as negative, weak (1+), moderate (2+), or strong (3+) based on DAB optical density thresholds.");
            plugin.setQueue("analysis.exchange");
            plugin.setTopic("job.stardist.ihc_positive_cells");
            plugin.setExampleArgs(Map.of(
                    "dab_threshold", "0.05",
                    "thresh_weak", "0.05",
                    "thresh_moderate", "0.15",
                    "thresh_strong", "0.3"
            ));
            plugin.setInstalledBy("system");
            plugin.setInstalledDate(LocalDateTime.now());
            plugin.setStatus(PluginStatus.INSTALLED);
            plugin.setReadme("""
                    # IHC Positive Cell Quantification Plugin

                    ## Overview
                    This plugin quantifies DAB-positive cells in IHC-stained histopathology images within a specified region of interest.

                    ## What it does
                    - Detects nuclei within the specified region
                    - Measures DAB optical density (OD) mean for each nucleus
                    - Classifies each nucleus as negative, weak (1+), moderate (2+), or strong (3+)
                    - Returns counts and percentages per category

                    ## Arguments
                    - `region` — region of interest as JSON `{"x":int,"y":int,"width":int,"height":int}` (required)
                    - `dab_threshold` — minimum DAB OD mean to classify a nucleus as positive (default: 0.05)
                    - `thresh_weak` — DAB mean ≥ this value → weak 1+ (default: 0.05)
                    - `thresh_moderate` — DAB mean ≥ this value → moderate 2+ (default: 0.15)
                    - `thresh_strong` — DAB mean ≥ this value → strong 3+ (default: 0.3)

                    ## Output
                    Positive cell counts and percentages broken down by staining intensity (negative, 1+, 2+, 3+).

                    ## Notes
                    - Designed for DAB/hematoxylin IHC staining
                    - Results are non-diagnostic and intended for research use only
                    """);
            pluginRepository.save(plugin);
            log.info("Default plugin 'ihc_positive_cells' created.");
        }
    }
}
