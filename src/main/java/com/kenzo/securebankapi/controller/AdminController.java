package com.kenzo.securebankapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;



import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // ✅ tells Spring this class handles HTTP requests
public class AdminController { // ✅ REQUIRED in Java 17

    @GetMapping("/internal-only")
    public String internalOnly(HttpServletRequest request) {

        // ❌ Step 1: Try to get client IP from header
        String forwardedFor = request.getHeader("X-Forwarded-For");

        // ❌ Step 2: Fallback to direct connection IP
        String remoteAddr = request.getRemoteAddr();

        // ❌ Step 3: Choose header if present
        String clientIp = (forwardedFor != null) ? forwardedFor : remoteAddr;

        // ❌ Step 4: Naive trust check
        if ("127.0.0.1".equals(clientIp)) {
            return "ACCESS GRANTED: Internal system";
        }

        return "ACCESS DENIED";
    }
}