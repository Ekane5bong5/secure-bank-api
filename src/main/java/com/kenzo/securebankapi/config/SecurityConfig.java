package com.kenzo.securebankapi.config;

import com.kenzo.securebankapi.filters.JWTTokenGeneratorFilter;
import com.kenzo.securebankapi.filters.JWTTokenValidatorFilter;
import com.kenzo.securebankapi.repository.UserRepository;
import com.kenzo.securebankapi.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // 🔥 Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔥 Connect Spring Security → YOUR DB USERS
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // 🔥 Tell Spring to load users from DB
        authProvider.setUserDetailsService(userDetailsService);

        // 🔥 Tell Spring how passwords are encoded
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    // 🔥 JWT Generator
    @Bean
    public JWTTokenGeneratorFilter jwtTokenGeneratorFilter(UserRepository userRepository) {
        return new JWTTokenGeneratorFilter(userRepository);
    }

    // 🔥 JWT Validator
    @Bean
    public JWTTokenValidatorFilter jwtTokenValidatorFilter() {
        return new JWTTokenValidatorFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JWTTokenGeneratorFilter jwtTokenGeneratorFilter,
                                                   JWTTokenValidatorFilter jwtTokenValidatorFilter,
                                                   DaoAuthenticationProvider authProvider)
            throws Exception {

        http
                // 🔥 Disable CSRF (needed for file uploads via curl)
                .csrf(csrf -> csrf.disable())

                // 🔥 Allow H2 console frames (otherwise blocked)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 🔥 Register authentication provider
                .authenticationProvider(authProvider)

                // 🔥 Stateless (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 🔓 LAB ACCESS
                        .requestMatchers("/files/**").permitAll()   // ⭐ CRITICAL FOR TODAY

                        // 🔓 Existing open endpoints
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/users").permitAll()
                        .requestMatchers("/internal-only").permitAll()

                        // 🔒 Everything else protected
                        .anyRequest().authenticated()
                )

                // 🔥 JWT validation first
                .addFilterBefore(jwtTokenValidatorFilter, BasicAuthenticationFilter.class)

                // 🔥 JWT generation after auth
                .addFilterAfter(jwtTokenGeneratorFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
}