package com.kenzo.securebankapi.config;

import com.kenzo.securebankapi.model.User;
import com.kenzo.securebankapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();

                admin.setUsername("admin"); // identity
                admin.setPassword(passwordEncoder.encode("password")); // authentication
                admin.setRole("ADMIN"); // authorization

// 🔥 CRITICAL — tenant boundary (this was missing and caused crash)
                admin.setTenantId("tenant_A");

                userRepository.save(admin); // persist user safely
            }
        };
    }
}
