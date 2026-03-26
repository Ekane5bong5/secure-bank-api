package com.kenzo.securebankapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerUsername;

    private Double balance;

    // 🔥 CRITICAL — tenant boundary at data level
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Double getBalance() {
        return balance;
    }

    public String getTenantId() {
        return tenantId;
    }

    // --- Setters ---

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
