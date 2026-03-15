package org.histovis.userservice.controller;

import jakarta.validation.Valid;
import org.histovis.commons.jwt.JwtUtil;
import org.histovis.userservice.dto.LoginRequest;
import org.histovis.userservice.dto.LoginResponse;
import org.histovis.userservice.dto.UserDto;
import org.histovis.userservice.model.User;
import org.histovis.userservice.repository.UserRepository;
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
@RequestMapping(Constants.AUTH_BASE_URL)
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwUtils;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authManager, JwtUtil jwUtils, UserRepository userRepository) {
        this.authManager = authManager;
        this.jwUtils = jwUtils;
        this.userRepository = userRepository;
    }

    @PostMapping(Constants.LOGIN_URL)
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid login credentials"));
            String token = jwUtils.generateToken(request.getUsername(), List.of(USER_ROLE));
            UserDto userDto = new UserDto(user.getUsername(), user.getFirstName(), user.getLastName(), user.isAdmin(), token);
            return new LoginResponse(userDto);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid login credentials");
        }
    }
}
