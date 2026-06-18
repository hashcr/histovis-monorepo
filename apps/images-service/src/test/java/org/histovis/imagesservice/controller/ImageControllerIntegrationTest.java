package org.histovis.imagesservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.histovis.commons.jwt.JwtUtil;
import org.histovis.imagesservice.model.Image;
import org.histovis.imagesservice.repository.ImageRepository;
import org.histovis.commons.storage.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ImageControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpass");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ObjectStorageService objectStorageService;

    private String validToken;

    @BeforeEach
    void setUp() {
        imageRepository.deleteAll();
        validToken = jwtUtil.generateToken("testuser", List.of("USER"));
        when(objectStorageService.upload(anyString(), any(), anyLong(), anyString()))
                .thenReturn("http://minio:9000/images/test-key");
        when(objectStorageService.getUrl(anyString()))
                .thenReturn("http://minio:9000/images/test-key");
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
        return headers;
    }

    // --- Search ---

    @Test
    void search_shouldReturn401_whenNotAuthenticated() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/images/search", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void search_shouldReturnImages_whenAuthenticated() {
        Image image = new Image();
        image.setFileName("sample.tiff");
        image.setStorageKey("images/uuid/sample.tiff");
        image.setPublicUrl("http://minio:9000/bucket/sample.tiff");
        image.setTitle("Sample");
        image.setDescription("A sample image");
        imageRepository.save(image);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/images/search", HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("images");
    }

    // --- Get Image ---

    @Test
    void getImage_shouldReturnImage_whenExists() {
        Image image = new Image();
        image.setFileName("test.tiff");
        image.setStorageKey("images/uuid/test.tiff");
        image.setPublicUrl("http://minio:9000/bucket/test.tiff");
        image.setTitle("Test");
        Image saved = imageRepository.save(image);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/images/" + saved.getId(), HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("image");
    }

    @Test
    void getImage_shouldReturn404_whenNotFound() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/images/" + UUID.randomUUID(), HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKey("error");
    }

    // --- Update Image ---

    @Test
    void updateImage_shouldUpdateMetadata_whenImageExists() {
        Image image = new Image();
        image.setFileName("original.tiff");
        image.setStorageKey("images/uuid/original.tiff");
        image.setPublicUrl("http://minio:9000/bucket/original.tiff");
        image.setTitle("Original Title");
        Image saved = imageRepository.save(image);

        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("title", "Updated Title");
        body.add("notes", "Some notes");

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/images/" + saved.getId(), HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("image");
    }

    @Test
    void updateImage_shouldReturn400_whenTagsListIsInvalidJson() {
        Image image = new Image();
        image.setFileName("test.tiff");
        image.setStorageKey("images/uuid/test.tiff");
        image.setPublicUrl("http://minio:9000/bucket/test.tiff");
        image.setTitle("Test");
        Image saved = imageRepository.save(image);

        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("tagsList", "not-valid-json");

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/images/" + saved.getId(), HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("error");
    }
}
