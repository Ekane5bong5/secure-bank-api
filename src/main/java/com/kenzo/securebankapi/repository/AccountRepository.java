package com.kenzo.securebankapi.repository;

import com.kenzo.securebankapi.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface AccountRepository extends JpaRepository<Account, Long> {

    // 🔥 Fetch account ONLY if it belongs to the given tenant
    Optional<Account> findByIdAndTenantId(Long id, String tenantId);
}

