package org.histovis.commons.jwt;

import org.histovis.commons.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestConfig.class)
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateAndValidateToken() {
        String username = "testuser";
        List<String> roles = List.of("USER");
        String token = jwtUtil.generateToken(username, roles);

        assertNotNull(token);
        assertEquals(username, jwtUtil.getUsernameFromToken(token));
        assertEquals(roles, jwtUtil.getRolesFromToken(token));
    }
}