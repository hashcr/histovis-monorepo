package org.histovis.userservice.controller;


import org.histovis.userservice.dto.LoginRequest;
import org.histovis.userservice.dto.LoginResponse;
import org.histovis.userservice.model.User;
import org.histovis.userservice.repository.UserRepository;
import org.histovis.userservice.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;


import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("testpass");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        // clean slate
        userRepository.deleteAll();
        // test user creation
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("123queso"));
        userRepository.save(user);
    }

    @Test
    void login_shouldReturnJwtToken_whenCredentialsAreOk() {

        // Create test data
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123queso");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(Constants.LOGIN_URL, entity,
                LoginResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    void login_shouldReturn401_whenCredentialsAreInvalid() {
        // Data
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("badpasswd");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(Constants.LOGIN_URL, entity,
                LoginResponse.class);

        // Asserts
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

}
