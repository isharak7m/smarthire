package com.smarthire;

import com.smarthire.model.User;
import com.smarthire.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
public class SmarthireApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmarthireApplication.class, args);
    }

    @Bean
    public CommandLineRunner ensureDemoAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User admin = userRepository.findByUsername("admin").orElseGet(User::new);
            admin.setUsername("admin");
            admin.setEmail("admin@smarthire.io");
            admin.setRole("ROLE_ADMIN");

            if (admin.getCreatedAt() == null) {
                admin.setCreatedAt(LocalDateTime.now());
            }

            String existingPassword = admin.getPassword() == null ? "" : admin.getPassword();
            if (!passwordEncoder.matches("Admin@123", existingPassword)) {
                admin.setPassword(passwordEncoder.encode("Admin@123"));
            }

            userRepository.save(admin);
        };
    }
}
