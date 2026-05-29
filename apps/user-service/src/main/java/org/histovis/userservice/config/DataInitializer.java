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
        seedUser("ashuin.sharma@gmail.com",        "Ashuin",     "Sharma",           true);
        seedUser("juan.porraspenaranda@ucr.ac.cr", "Juan",       "Porras Peñaranda", false);
        seedUser("joseluis.quiros@ucr.ac.cr",      "Jose Luis",  "Quirós",           false);
        seedUser("oscar.moraduran@ucr.ac.cr",      "Oscar",      "Mora Durán",       false);
        seedUser("danielgutiear@gmail.com",         "Daniel",     "Gutiérrez",        false);
        seedUser("gustavo.lopezherrera@ucr.ac.cr", "Gustavo",    "López Herrera",    false);
        seedUser("alexandra.martinez@ucr.ac.cr",   "Alexandra",  "Martinez Porras",  false);
    }

    private void seedUser(String email, String firstName, String lastName, boolean isAdmin) {
        if (userRepository.findByUsername(email).isEmpty()) {
            User u = new User();
            u.setUsername(email);
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setPassword(passwordEncoder.encode("123queso"));
            u.setAdmin(isAdmin);
            userRepository.save(u);
            log.info("Seeded user: {}", email);
        }
    }
}
