package org.histovis.userservice.controller;

import jakarta.validation.Valid;
import org.histovis.commons.jwt.JwtUtil;
import org.histovis.userservice.dto.LoginRequest;
import org.histovis.userservice.dto.LoginResponse;
import org.histovis.userservice.utils.Constants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.histovis.userservice.utils.Constants.*;

@RestController
@RequestMapping(Constants.LOGIN_URL)
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwUtils;

    public AuthController(AuthenticationManager authManager, JwtUtil jwUtils) {
        this.authManager = authManager;
        this.jwUtils = jwUtils;
    }

    @PostMapping
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            String token = jwUtils.generateToken(request.getUsername(), List.of(USER_ROLE));
            return new LoginResponse(token);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
