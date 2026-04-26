package org.histovis.analysisservice.controller;

import org.histovis.analysisservice.common.JobStatus;
import org.histovis.analysisservice.model.Job;
import org.histovis.analysisservice.repository.JobRepository;
import org.histovis.commons.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class JobControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpass");

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private UUID testImageId;

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        userToken = jwtUtil.generateToken("user@example.com", List.of("USER"));
        testImageId = UUID.randomUUID();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Job savedJob(UUID imageId) {
        Job job = new Job();
        job.setPluginCode("test-plugin");
        job.setImageId(imageId);
        job.setImageUrl("http://minio:9000/bucket/image.jpg");
        job.setArgs(Map.of("key", "value"));
        job.setStatus(JobStatus.PENDING);
        job.setDate(LocalDateTime.now());
        job.setUsername("user@example.com");
        return jobRepository.save(job);
    }

    // --- Submit Job ---

    @Test
    void submit_shouldReturn201_withValidRequest() {
        String body = """
                {
                  "pluginCode": "describe_wsi",
                  "imageId": "%s",
                  "imageUrl": "http://minio:9000/bucket/image.jpg",
                  "args": { "threshold": "0.5" }
                }
                """.formatted(testImageId);

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/analysis/jobs", entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("id");
    }

    @Test
    void submit_shouldReturn400_whenPluginCodeMissing() {
        String body = """
                {
                  "imageId": "%s",
                  "imageUrl": "http://minio:9000/bucket/image.jpg"
                }
                """.formatted(testImageId);

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/analysis/jobs", entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("pluginCode");
    }

    @Test
    void submit_shouldReturn400_whenImageIdMissing() {
        String body = """
                {
                  "pluginCode": "cell-segmentation",
                  "imageUrl": "http://minio:9000/bucket/image.jpg"
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/analysis/jobs", entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("imageId");
    }

    @Test
    void submit_shouldReturn401_whenNoToken() {
        String body = """
                {
                  "pluginCode": "cell-segmentation",
                  "imageId": "%s",
                  "imageUrl": "http://minio:9000/bucket/image.jpg"
                }
                """.formatted(testImageId);

        HttpEntity<String> entity = new HttpEntity<>(body, new HttpHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/analysis/jobs", entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- List Jobs ---

    @Test
    void list_shouldReturnJobsForImage() {
        savedJob(testImageId);
        savedJob(testImageId);
        savedJob(UUID.randomUUID()); // different image, should not appear

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/analysis/jobs?imageId=" + testImageId, HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> jobs = (List<?>) response.getBody().get("jobs");
        assertThat(jobs).hasSize(2);
    }

    @Test
    void list_shouldReturnEmptyList_whenNoJobsForImage() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/analysis/jobs?imageId=" + testImageId, HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> jobs = (List<?>) response.getBody().get("jobs");
        assertThat(jobs).isEmpty();
    }

    // --- Get Job ---

    @Test
    void get_shouldReturnJob_whenExists() {
        Job job = savedJob(testImageId);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/analysis/jobs/" + job.getId(), HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("job");
    }

    @Test
    void get_shouldReturn404_whenNotFound() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/analysis/jobs/" + UUID.randomUUID(), HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- Update Job Result ---

    @Test
    void updateResult_shouldReturn204_whenJobExists() {
        Job job = savedJob(testImageId);

        String body = """
                {
                  "status": "COMPLETED",
                  "output": "Analysis complete: tissue is healthy."
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/analysis/jobs/" + job.getId() + "/result", HttpMethod.PUT, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Job updated = jobRepository.findById(job.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(updated.getOutput()).isEqualTo("Analysis complete: tissue is healthy.");
        assertThat(updated.getCompletedDate()).isNotNull();
    }

    @Test
    void updateResult_shouldReturn404_whenJobNotFound() {
        String body = """
                {
                  "status": "FAILED",
                  "output": "Worker error."
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/analysis/jobs/" + UUID.randomUUID() + "/result", HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
