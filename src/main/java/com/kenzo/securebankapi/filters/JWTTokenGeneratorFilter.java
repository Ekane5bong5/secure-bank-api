package com.kenzo.securebankapi.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

import io.jsonwebtoken.Jwts;

import com.kenzo.securebankapi.model.User;
import com.kenzo.securebankapi.repository.UserRepository;

import static com.kenzo.securebankapi.config.JwtKeyProvider.JWT_KEY;

public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    // 🔥 Injected dependency (via SecurityConfig bean)
    private final UserRepository userRepository;

    // 🔥 Constructor injection
    public JWTTokenGeneratorFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 🔹 Step 1: Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 🔹 Step 2: Only proceed if authenticated
        if (authentication != null && authentication.isAuthenticated()) {

            String username = authentication.getName();

            // 🔥 Fetch user from DB (ONLY at token generation time)
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String tenantId = user.getTenantId();

            // 🔥 Build JWT with tenantId embedded
            String jwt = Jwts.builder()
                    .subject(username)
                    .claim("authorities", authentication.getAuthorities().toString())
                    .claim("tenantId", tenantId) // 🔥 CRITICAL
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 300000))
                    .signWith(JWT_KEY)
                    .compact();

            // 🔥 Send token back to client
            response.setHeader("Authorization", "Bearer " + jwt);

            System.out.println("JWT generated for user: " + username + " | tenant: " + tenantId);
        }

        filterChain.doFilter(request, response);
    }
}