package com.kenzo.securebankapi.config;

import com.kenzo.securebankapi.filters.JWTTokenGeneratorFilter;
import com.kenzo.securebankapi.filters.JWTTokenValidatorFilter;
import com.kenzo.securebankapi.repository.UserRepository;
import com.kenzo.securebankapi.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public JWTTokenGeneratorFilter jwtTokenGeneratorFilter(UserRepository userRepository) {
        return new JWTTokenGeneratorFilter(userRepository);
    }

    @Bean
    public JWTTokenValidatorFilter jwtTokenValidatorFilter() {
        return new JWTTokenValidatorFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JWTTokenGeneratorFilter jwtTokenGeneratorFilter,
            JWTTokenValidatorFilter jwtTokenValidatorFilter,
            DaoAuthenticationProvider authProvider
    ) throws Exception {

        http
                // 🔥 API style → no CSRF
                .csrf(csrf -> csrf.disable())

                // 🔥 Allow H2 console frames
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 🔥 Stateless (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔥 Plug in DB auth
                .authenticationProvider(authProvider)

                // 🔥 Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/users").permitAll()
                        .requestMatchers("/internal-only").permitAll()
                        .anyRequest().permitAll()
                )

                // 🔥 Custom entry point (only triggers when auth is REQUIRED)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                )

                // 🔥 Filters
                .addFilterBefore(jwtTokenValidatorFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(jwtTokenGeneratorFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
}