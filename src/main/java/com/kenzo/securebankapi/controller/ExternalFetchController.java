package com.kenzo.securebankapi.controller;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.URI;

@RestController // Marks this class as a REST API controller (handles HTTP requests)
@RequestMapping("/api/external") // Base URL path for all endpoints in this controller
public class ExternalFetchController {

    // RestTemplate is a Spring utility that allows us to make HTTP requests to other servers
    private final RestTemplate restTemplate;

    public ExternalFetchController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(3000); // ⏱️ Prevent long hangs
        factory.setReadTimeout(3000);    // ⏱️ Prevent slow responses

        this.restTemplate = new RestTemplate(factory);
    }

    @GetMapping("/fetch")
    public ResponseEntity<String> fetchExternal(@RequestParam String url) {

        try {
            // 🔍 Step 1: Parse input
            URI uri = new URI(url);

            // 🔍 Step 2: Extract host
            String host = uri.getHost();

            // 🚫 Step 3: Basic string validation
            if (host == null ||
                    host.equals("localhost") ||
                    host.startsWith("127.") ||
                    host.equals("0.0.0.0") ||
                    host.equals("169.254.169.254")) {

                return ResponseEntity.badRequest().body("Invalid target");
            }

            // 🔥 Step 4: Resolve hostname → IP
            InetAddress address = InetAddress.getByName(host);

            // 🚫 Step 5: Block internal/private ranges
            if (address.isAnyLocalAddress() ||
                    address.isLoopbackAddress() ||
                    address.isSiteLocalAddress()) {

                return ResponseEntity.badRequest().body("Blocked internal address");
            }

            // 🔒 Step 6: LOCK the resolved IP (prevents DNS rebinding)
            String ip = address.getHostAddress();

            URI safeUri = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    ip,              // 🔒 Use IP instead of hostname
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            );

            // 💣 Step 7: MAKE REQUEST (REPLACES getForObject)
            ResponseEntity<String> response = restTemplate.exchange(
                    safeUri,               // ✅ use locked IP version
                    HttpMethod.GET,
                    null,
                    String.class
            );

            // 🚫 Step 8: Block redirects manually
            if (response.getStatusCode().is3xxRedirection()) {
                return ResponseEntity.badRequest().body("Redirects not allowed");
            }

            // ✅ Step 9: Return response
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}