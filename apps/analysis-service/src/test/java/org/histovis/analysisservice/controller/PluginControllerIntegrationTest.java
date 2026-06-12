package org.histovis.analysisservice.controller;

import org.histovis.analysisservice.common.PluginStatus;
import org.histovis.analysisservice.model.Plugin;
import org.histovis.analysisservice.repository.PluginRepository;
import org.histovis.commons.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PluginControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpass");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PluginRepository pluginRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        pluginRepository.deleteAll();
        userToken = jwtUtil.generateToken("user@example.com", List.of("USER"));
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        return headers;
    }

    private Plugin savedPlugin(String code) {
        Plugin plugin = new Plugin();
        plugin.setCode(code);
        plugin.setName("Test Plugin " + code);
        plugin.setQueue("test-queue");
        plugin.setTopic("test-topic");
        plugin.setInstalledBy("admin@example.com");
        plugin.setInstalledDate(LocalDateTime.now());
        plugin.setStatus(PluginStatus.INSTALLED);
        return pluginRepository.save(plugin);
    }

    // --- List Plugins ---

    @Test
    void list_shouldReturnEmptyList_whenNoPlugins() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/analysis/plugins", HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> plugins = (List<?>) response.getBody().get("plugins");
        assertThat(plugins).isEmpty();
    }

    @Test
    void list_shouldReturnAllPlugins_whenExist() {
        savedPlugin("cell-segmentation");
        savedPlugin("nuclei-detection");

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/analysis/plugins", HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> plugins = (List<?>) response.getBody().get("plugins");
        assertThat(plugins).hasSize(2);
    }

    @Test
    void list_shouldReturn401_whenNoToken() {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/analysis/plugins", HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
