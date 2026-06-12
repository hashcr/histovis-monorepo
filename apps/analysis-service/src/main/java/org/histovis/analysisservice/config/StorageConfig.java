package org.histovis.analysisservice.config;

import org.histovis.commons.config.StorageAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(StorageAutoConfiguration.class)
public class StorageConfig {
}
