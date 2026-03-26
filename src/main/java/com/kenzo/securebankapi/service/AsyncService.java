package com.kenzo.securebankapi.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {

    @Async
    public void printCurrentUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        System.out.println("Async thread authentication: " + auth);

    }
}
