package com.kenzo.securebankapi.service;

import com.kenzo.securebankapi.model.User;
import com.kenzo.securebankapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("permitAll()")
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getTenantId() == null) {
            user.setTenantId("tenant_A"); // default fallback
        }
        return userRepository.save(user);
    }

    @PreAuthorize("#id == authentication.name or hasRole('ADMIN')")
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User internalLookup(Long id) {
        return getUser(id);
    }

    // 🔥 PURPOSE:
// Given the currently authenticated user,
// return their tenantId from the database (trusted source)
    public String getCurrentUserTenant() {

        // 🔹 Step 1:
        // Get username from SecurityContext (trusted identity)
        String username = com.kenzo.securebankapi.security.SecurityUtils.getCurrentUsername();

        // 🔹 Step 2:
        // Look up user in database using username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔹 Step 3:
        // Extract tenantId from the user record
        // This is now trusted because it comes from DB, not client input
        return user.getTenantId();
    }
}
