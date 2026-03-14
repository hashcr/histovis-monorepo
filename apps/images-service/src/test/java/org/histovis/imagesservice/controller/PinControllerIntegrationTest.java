package org.histovis.imagesservice.controller;

import org.histovis.commons.jwt.JwtUtil;
import org.histovis.imagesservice.model.Image;
import org.histovis.imagesservice.model.Pin;
import org.histovis.imagesservice.repository.ImageRepository;
import org.histovis.imagesservice.repository.PinRepository;
import org.histovis.imagesservice.storage.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
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

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PinControllerIntegrationTest {

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
    private PinRepository pinRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ObjectStorageService objectStorageService;

    private Image testImage;
    private String userToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        pinRepository.deleteAll();
        imageRepository.deleteAll();

        testImage = new Image();
        testImage.setFileName("slide.tiff");
        testImage.setStorageKey("images/uuid/slide.tiff");
        testImage.setPublicUrl("http://minio:9000/bucket/slide.tiff");
        testImage.setTitle("Slide");
        testImage = imageRepository.save(testImage);

        userToken = jwtUtil.generateToken("user@example.com", List.of("USER"));
        otherToken = jwtUtil.generateToken("other@example.com", List.of("USER"));
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String pinsUrl() {
        return "/api/images/" + testImage.getId() + "/pins";
    }

    // --- Create Pin ---

    @Test
    void createPin_shouldReturn201_withValidData() {
        String body = """
                { "pin": { "isPublic": true, "email": "user@example.com", "x": 0.5, "y": 0.3, "zoom": 1.0, "text": "test pin", "sequence_id": 1 } }
                """;

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders(userToken));
        ResponseEntity<Map> response = restTemplate.postForEntity(pinsUrl(), entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKey("pin");
    }

    @Test
    void createPin_shouldReturn400_whenXOrYMissing() {
        String body = """
                { "pin": { "isPublic": true, "email": "user@example.com", "zoom": 1.0 } }
                """;

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders(userToken));
        ResponseEntity<Map> response = restTemplate.postForEntity(pinsUrl(), entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- Update Pin ---

    @Test
    void updatePin_shouldUpdateFields() {
        Pin pin = new Pin();
        pin.setImage(testImage);
        pin.setOwnerEmail("user@example.com");
        pin.setIsPublic(false);
        pin.setX(0.1);
        pin.setY(0.2);
        pin = pinRepository.save(pin);

        String body = """
                { "pin": { "x": 0.9, "y": 0.8, "text": "updated" } }
                """;

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders(userToken));
        ResponseEntity<Map> response = restTemplate.exchange(
                pinsUrl() + "/" + pin.getId(), HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("pin");
    }

    @Test
    void updatePin_shouldReturn404_whenNotFound() {
        String body = """
                { "pin": { "x": 0.5, "y": 0.5 } }
                """;

        HttpEntity<String> entity = new HttpEntity<>(body, authHeaders(userToken));
        ResponseEntity<Map> response = restTemplate.exchange(
                pinsUrl() + "/" + UUID.randomUUID(), HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- Delete Pin ---

    @Test
    void deletePin_shouldReturn200_withDeletedTrue() {
        Pin pin = new Pin();
        pin.setImage(testImage);
        pin.setOwnerEmail("user@example.com");
        pin.setIsPublic(true);
        pin.setX(0.5);
        pin.setY(0.5);
        pin = pinRepository.save(pin);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(userToken));
        ResponseEntity<Map> response = restTemplate.exchange(
                pinsUrl() + "/" + pin.getId(), HttpMethod.DELETE, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("deleted")).isEqualTo(true);
        assertThat(response.getBody().get("pins")).isNotNull();
    }

    // --- Pin visibility filtering ---

    @Test
    void getPins_shouldReturnOnlyPublicAndOwnedPins() {
        // public pin (visible to everyone)
        Pin publicPin = new Pin();
        publicPin.setImage(testImage);
        publicPin.setOwnerEmail("other@example.com");
        publicPin.setIsPublic(true);
        publicPin.setX(0.1);
        publicPin.setY(0.1);
        pinRepository.save(publicPin);

        // private pin owned by user@example.com
        Pin ownPin = new Pin();
        ownPin.setImage(testImage);
        ownPin.setOwnerEmail("user@example.com");
        ownPin.setIsPublic(false);
        ownPin.setX(0.2);
        ownPin.setY(0.2);
        pinRepository.save(ownPin);

        // private pin owned by other (should NOT be visible)
        Pin otherPrivatePin = new Pin();
        otherPrivatePin.setImage(testImage);
        otherPrivatePin.setOwnerEmail("other@example.com");
        otherPrivatePin.setIsPublic(false);
        otherPrivatePin.setX(0.3);
        otherPrivatePin.setY(0.3);
        pinRepository.save(otherPrivatePin);

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(userToken));
        ResponseEntity<Map> response = restTemplate.exchange(pinsUrl(), HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> pins = (List<?>) response.getBody().get("pins");
        // public pin + own private pin = 2, NOT the other private pin
        assertThat(pins).hasSize(2);
    }
}
