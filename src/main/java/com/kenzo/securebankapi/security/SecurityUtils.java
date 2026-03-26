package com.kenzo.securebankapi.security;

// Import Spring Security's Authentication object
// This represents the currently authenticated user (who made the request)
import org.springframework.security.core.Authentication;

// Import SecurityContextHolder
// This is where Spring stores authentication info for the current request thread
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    // 🔥 PURPOSE:
    // This method retrieves the CURRENT authenticated user's username
    // from Spring Security's context (NOT from user input)
    public static String getCurrentUsername() {

        // 🔹 Step 1:
        // Get the current Authentication object from SecurityContext
        // This is populated by your auth filters (JWT or Basic Auth)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔹 Step 2:
        // Validate that authentication exists
        // If this is null → no user is logged in → something is wrong
        if (auth == null) {
            throw new RuntimeException("No authentication found in SecurityContext");
        }

        // 🔹 Step 3:
        // Extract username from Authentication
        // auth.getName() = username (e.g., "admin")
        String username = auth.getName();

        // 🔹 Step 4:
        // Defensive check — username should NEVER be null if authenticated
        if (username == null) {
            throw new RuntimeException("Authenticated user has no username");
        }

        // 🔹 Step 5:
        // Return trusted username (NOT from request body, NOT from client)
        return username;
    }
}
