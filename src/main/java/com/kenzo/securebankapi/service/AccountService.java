package com.kenzo.securebankapi.service;

import com.kenzo.securebankapi.model.Account;
import com.kenzo.securebankapi.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final UserService userService; // 🔥 Needed to fetch current tenant

    public AccountService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    public Account createAccount(Account account) {

        // 🔥 TEMP — we are hardcoding tenant (same pattern as User earlier)
        if (account.getTenantId() == null) {
            account.setTenantId("tenant_A");
        }

        return accountRepository.save(account);
    }

    public Account getAccount(Long id) {

        // 🔹 Get current tenant (trusted)
        String currentTenant = userService.getCurrentUserTenant();

        // 🔹 Fetch account ONLY if tenant matches
        return accountRepository.findByIdAndTenantId(id, currentTenant)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }
}
