package org.histovis.userservice.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class DockerEnvironmentTest {
    
    @Container
    public GenericContainer<?> container = new GenericContainer<>("hello-world:latest");
    
    @Test
    void testDockerEnvironment() {
        container.start();
        // If this test passes, your Docker environment is working correctly
    }
}