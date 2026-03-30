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

        // 🔹 Step 1: Read request path
        String path = request.getServletPath();

        // 🔥 Skip JWT validation for PUBLIC endpoints
        if (path.startsWith("/files") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/users") ||
                path.startsWith("/internal-only")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Step 2: Read Authorization header
        String jwt = request.getHeader("Authorization");

        // 🔥 CRITICAL: No JWT → allow request to continue
        if (jwt == null || !jwt.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 🔹 Remove "Bearer "
            jwt = jwt.substring(7);

            // 🔥 Parse and validate JWT
            Claims claims = Jwts.parser()
                    .verifyWith(JWT_KEY)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            String username = claims.getSubject();
            Object authoritiesClaim = claims.get("authorities");
            String tenantId = claims.get("tenantId", String.class);

            // 🔹 Validate required claims
            if (username == null || username.isBlank()) {
                throw new InsufficientAuthenticationException("Missing subject claim");
            }

            if (authoritiesClaim == null) {
                throw new InsufficientAuthenticationException("Missing authorities claim");
            }

            if (tenantId == null || tenantId.isBlank()) {
                throw new InsufficientAuthenticationException("Missing tenantId claim");
            }

            // 🔹 Convert authorities
            List<GrantedAuthority> authorities =
                    AuthorityUtils.commaSeparatedStringToAuthorityList(
                            authoritiesClaim.toString()
                    );

            // 🔹 Create Authentication
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
            );

            // 🔥 Attach tenantId
            ((UsernamePasswordAuthenticationToken) auth).setDetails(tenantId);

            SecurityContextHolder.getContext().setAuthentication(auth);

            System.out.println("JWT authenticated user: " + username + " | tenant: " + tenantId);

        } catch (Exception ex) {

            // 🔥 Invalid JWT → reject
            SecurityContextHolder.clearContext();

            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Invalid JWT token", ex)
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}