package com.kenzo.securebankapi.controller;

// Import Account model (represents account data)
import com.kenzo.securebankapi.model.Account;

// Import service layer (where business logic lives)
import com.kenzo.securebankapi.service.AccountService;

// Spring annotation to mark this as a REST controller (handles HTTP requests)
import org.springframework.web.bind.annotation.*;

@RestController // Tells Spring: this class handles API requests
@RequestMapping("/accounts") // Base URL for all endpoints in this class
public class AccountController {

    // Service that contains account logic (create, fetch, etc.)
    private final AccountService accountService;

    // Constructor injection (Spring automatically provides AccountService)
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // 🔥 CREATE ACCOUNT ENDPOINT
    @PostMapping
    public Account createAccount(@RequestBody Account account) {

        // @RequestBody:
        // Takes incoming JSON and converts it into an Account object

        // Example request body:
        // {
        //   "ownerUsername": "alice",
        //   "balance": 1000,
        //   "tenantId": "tenant_A"
        // }

        return accountService.createAccount(account);

        // Calls service layer to:
        // - set tenantId if missing
        // - save account to DB
    }

    // 🔥 GET ACCOUNT BY ID
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {

        // @PathVariable:
        // Extracts the "id" from URL
        // Example: /accounts/1 → id = 1

        return accountService.getAccount(id);

        // Calls service which:
        // - fetches account from DB
        // - returns it WITHOUT any tenant validation (⚠️ vulnerability)
    }
}