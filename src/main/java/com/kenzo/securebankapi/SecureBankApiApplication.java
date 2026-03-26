package com.kenzo.securebankapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableMethodSecurity
@SpringBootApplication
@EnableAsync
public class SecureBankApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureBankApiApplication.class, args);
    }
}