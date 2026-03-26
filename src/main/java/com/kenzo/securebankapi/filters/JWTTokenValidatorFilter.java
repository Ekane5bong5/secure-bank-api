package com.kenzo.securebankapi.filters;

import com.kenzo.securebankapi.config.CustomAuthenticationEntryPoint;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.kenzo.securebankapi.config.JwtKeyProvider.JWT_KEY;

public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    private final AuthenticationEntryPoint authenticationEntryPoint =
            new CustomAuthenticationEntryPoint();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 🔹 Step 1: Read Authorization header
        String jwt = request.getHeader("Authorization");

        // 🔹 Step 2: Check if header exists and is Bearer token
        if (jwt != null && jwt.startsWith("Bearer ")) {
            try {

                // 🔹 Step 3: Remove "Bearer " prefix
                jwt = jwt.substring(7);

                // 🔥 Step 4: Parse and validate JWT (signature + expiration)
                Claims claims = Jwts.parser()
                        .verifyWith(JWT_KEY) // ensures token was signed by server
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

                // 🔹 Step 5: Extract identity
                String username = claims.getSubject();

                // 🔹 Step 6: Extract authorities (roles)
                Object authoritiesClaim = claims.get("authorities");

                // 🔥 NEW — Step 7: Extract tenantId from JWT
                String tenantId = claims.get("tenantId", String.class);

                // 🔹 Step 8: Validate required claims
                if (username == null || username.isBlank()) {
                    throw new InsufficientAuthenticationException("Missing subject claim");
                }

                if (authoritiesClaim == null) {
                    throw new InsufficientAuthenticationException("Missing authorities claim");
                }

                if (tenantId == null || tenantId.isBlank()) {
                    throw new InsufficientAuthenticationException("Missing tenantId claim");
                }

                // 🔹 Step 9: Convert authorities string → Spring Security roles
                List<GrantedAuthority> authorities =
                        AuthorityUtils.commaSeparatedStringToAuthorityList(
                                authoritiesClaim.toString()
                        );

                // 🔥 Step 10: Create Authentication object
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

                // 🔥 CRITICAL STEP:
                // Attach tenantId to Authentication
                // This makes tenant available globally via SecurityContext
                ((UsernamePasswordAuthenticationToken) auth).setDetails(tenantId);

                // 🔹 Step 11: Store authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 🔹 Debug log
                System.out.println("JWT authenticated user: " + username + " | tenant: " + tenantId);

            } catch (InsufficientAuthenticationException ex) {

                // 🔹 Missing required claims → clear context + reject
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(request, response, ex);
                return;

            } catch (Exception ex) {

                // 🔹 Invalid token → clear context + reject
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(
                        request,
                        response,
                        new BadCredentialsException("Invalid JWT token", ex)
                );
                return;
            }
        }

        // 🔹 Continue request processing
        filterChain.doFilter(request, response);
    }
}