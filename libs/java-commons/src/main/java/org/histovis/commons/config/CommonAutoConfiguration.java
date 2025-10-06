package org.histovis.commons.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.histovis.commons")
public class CommonAutoConfiguration {
    // Empty. This is just for self-registering.
}
