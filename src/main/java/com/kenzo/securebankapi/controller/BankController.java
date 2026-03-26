package com.kenzo.securebankapi.controller;

import com.kenzo.securebankapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kenzo.securebankapi.model.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Map;
import org.springframework.security.core.Authentication;
import java.util.HashMap;
import com.kenzo.securebankapi.service.AsyncService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.kenzo.securebankapi.dto.CreateUserRequest;

@RestController
public class BankController {

    @Autowired
    private AsyncService asyncService;

    private final UserService userService;

    public BankController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/account")
    public String account() {
        return "Account details";
    }

    @GetMapping("/admin")
    public String admin() {
        return "Admin panel";
    }
    @PostMapping("/users")
    public User createUser(@RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole("USER");

        return userService.createUser(user);
    }
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id, Authentication authentication) {

        User requestedUser = userService.getUser(id);

        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !requestedUser.getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own user record");
        }

        return requestedUser;
    }
    @GetMapping("/internal/users/{id}")
    public User internalGetUser(@PathVariable Long id) {
        return userService.internalLookup(id);
    }
    @GetMapping("/headers")
    public String inspectHeaders(@RequestHeader Map<String, String> headers) {

        headers.forEach((key, value) -> {

            if (key.equalsIgnoreCase("authorization")) {
                System.out.println(key + " : [REDACTED]");
            } else {
                System.out.println(key + " : " + value);
            }

        });

        return "Headers logged safely";
    }
    @GetMapping("/whoami")
    public Map<String, Object> whoAmI(Authentication authentication) {

        System.out.println("Authentication object: " + authentication);

        Map<String, Object> result = new HashMap<>();

        result.put("username", authentication.getName());
        result.put("authorities", authentication.getAuthorities());
        result.put("authenticated", authentication.isAuthenticated());
        result.put("principal", authentication.getPrincipal());

        return result;
    }
    @GetMapping("/async-test")
    public String asyncTest(Authentication authentication) {

        System.out.println("Controller thread authentication: " + authentication);

        asyncService.printCurrentUser();

        return "Async test triggered";
    }
}