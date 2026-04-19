package org.histovis.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.histovis.userservice.model.User;
import org.histovis.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("ashuin.sharma@gmail.com");
            admin.setFirstName("Ashuin");
            admin.setLastName("Sharma");
            admin.setPassword(passwordEncoder.encode("123queso"));
            admin.setAdmin(true);
            userRepository.save(admin);
            log.info("Default admin user created.");
        }
    }
}
