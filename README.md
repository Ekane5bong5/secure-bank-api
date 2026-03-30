# 🔐 Secure Bank API — Application Security Engineering Lab

## 📌 Overview

Secure Bank API is a **Spring Boot + Spring Security application security lab** designed to model, exploit, and remediate real-world backend vulnerabilities.

This project is not a production banking system.
It is a **hands-on AppSec training environment** focused on understanding how security failures occur at the code, service, and architecture levels.

The core philosophy:

> Build → Break → Analyze → Fix → Articulate

---

## 🎯 What This Project Demonstrates

This lab focuses on **real backend security concepts**, not just surface-level vulnerabilities.

### 🔑 Authentication & Authorization

* Custom `DaoAuthenticationProvider` with DB-backed users
* BCrypt password hashing
* Stateless JWT-based authentication
* SecurityContext usage across request lifecycle
* Method-level authorization (`@EnableMethodSecurity`)

---

### 🛡️ Vulnerability Modeling & Remediation

#### 🔥 SSRF (Server-Side Request Forgery)

* Endpoint: `/api/external/fetch`
* Demonstrates:

  * Hostname parsing & validation
  * Internal IP detection (loopback, private ranges)
  * DNS resolution & rebinding protection
  * Redirect handling
  * Timeout controls

---

#### 📂 File Upload Abuse

* Endpoint: `/files/upload`
* Demonstrates:

  * Filename normalization
  * Extension allowlisting
  * Path traversal prevention
  * Storage isolation (`/tmp/uploads`)
  * Size constraints (application + config level)

---

#### 🔐 Broken Access Control (IDOR / BOLA)

* Endpoint: `/users/{id}`
* Demonstrates:

  * Ownership validation using authenticated identity
  * Role-based access (USER vs ADMIN)
  * Service-layer authorization enforcement

---

#### 🧠 Trust Boundary Failures (Header Spoofing)

* Endpoint: `/internal-only`
* Demonstrates:

  * Insecure trust of `X-Forwarded-For`
  * Reverse proxy boundary assumptions
  * How header spoofing leads to privilege bypass

---

#### 🏦 Business Logic Vulnerabilities

* Endpoint: `/accounts/{id}`
* Demonstrates:

  * Missing tenant/ownership validation
  * Direct object reference risk
  * Service-layer trust flaws

---

#### ⚙️ SecurityContext & Async Behavior

* Endpoints:

  * `/whoami`
  * `/async-test`
* Demonstrates:

  * Authentication object structure
  * Thread-local SecurityContext behavior
  * Context loss in async execution

---

## ⚠️ Intentional Insecure Configurations

This project intentionally includes insecure patterns for learning purposes.

Examples:

* `/files/**` exposed for file upload testing
* `/internal-only` trusts client-controlled headers
* Some endpoints bypass strict authorization
* CSRF protection disabled for stateless JWT testing and exploit modeling

Each insecure configuration is:

1. Introduced intentionally
2. Exploited during testing
3. Hardened through iterative improvements

⚠️ This project is **not intended for production use**

---

## 🧪 Key Endpoints

```http
POST   /users
GET    /users/{id}
GET    /internal/users/{id}

POST   /files/upload

GET    /api/external/fetch?url=

GET    /accounts/{id}
POST   /accounts

GET    /internal-only

GET    /whoami
GET    /async-test
GET    /headers
```

---

## ⚙️ Tech Stack

* Java 17
* Spring Boot 3
* Spring Security
* JWT (jjwt)
* Spring Data JPA
* H2 In-Memory Database

---

## ▶️ Running the Project

```bash
./mvnw spring-boot:run
```

### Default Behavior

* H2 in-memory database
* JWT-based authentication flow
* File uploads stored in `/tmp/uploads`

---

## 🧠 Learning Goals

This project is designed to build deep understanding of:

* How authentication differs from authorization
* Where access control breaks in real systems
* How user-controlled input propagates through backend layers
* How trust boundaries fail (headers, proxies, external requests)
* How to design secure service-layer logic
* How to think like an attacker and a defender

---

## 🚀 Roadmap (In Progress)

* Path Traversal modeling & canonicalization
* Containerization (Docker)
* Reverse proxy simulation & trust boundary enforcement
* Cloud deployment (secure vs vulnerable configurations)
* Service-to-service authentication modeling
* Observability & security logging

---

## 🧭 Author

Built by an Application Security Engineer focused on:

* Backend security architecture
* Vulnerability discovery & remediation
* Secure system design in enterprise environments

---

## 🚀 Upcoming Enhancements (Phase 2)

- Docker containerization and hardening
- Cloud security mapping (IAM, network segmentation)
- DevSecOps pipeline enforcement (SAST, DAST, policy gates)
- Microservices expansion (audit-service)
- Distributed system threat modeling

## 🎯 Purpose

This project is part of a structured training plan to reach senior-level application security expertise by simulating enterprise-grade systems and attack scenarios.
