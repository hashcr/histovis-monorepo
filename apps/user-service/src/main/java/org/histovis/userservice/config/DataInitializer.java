package org.histovis.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.histovis.userservice.model.User;
import org.histovis.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SEED_EMAIL}")
    private String seedEmail;

    @Value("${SEED_PASSWORD}")
    private String seedPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser(seedEmail,                                    "Ashuin",     "Sharma",           true);
        seedUser("juan.porraspenaranda@histovis.online",       "Juan",       "Porras Peñaranda", false);
        seedUser("joseluis.quiros@histovis.online",            "Jose Luis",  "Quirós",           false);
        seedUser("oscar.moraduran@histovis.online",            "Oscar",      "Mora Durán",       false);
        seedUser("danielgutiear@histovis.online",              "Daniel",     "Gutiérrez",        false);
        seedUser("gustavo.lopezherrera@histovis.online",       "Gustavo",    "López Herrera",    false);
        seedUser("alexandra.martinez@histovis.online",         "Alexandra",  "Martinez Porras",  false);
    }

    private void seedUser(String email, String firstName, String lastName, boolean isAdmin) {
        if (userRepository.findByUsername(email).isEmpty()) {
            User u = new User();
            u.setUsername(email);
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setPassword(passwordEncoder.encode(seedPassword));
            u.setAdmin(isAdmin);
            userRepository.save(u);
            log.info("Seeded user: {}", email);
        }
    }
}
