# PULSE — Phone-based User Login & Security Engine

A reusable authentication foundation for Spring Boot apps that verifies users via **phone number + OTP (6-digit PIN)** and authenticates with **JWT**. Built with **Java 21**, **Spring Boot 3.5.5**, **Spring Security**, and **MongoDB**.

**Repository:** `pulse-auth`  
**Base package (current code):** `org.cheetah.pulse`

---

## Table of Contents

- [Why PULSE?](#why-pulse)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Data Model](#data-model)
- [Authentication Flows](#authentication-flows)
  - [Registration with OTP](#registration-with-otp)
  - [Login (JWT)](#login-jwt)
  - [Password Reset (OTP → short-lived reset token)](#password-reset-otp--short-lived-reset-token)
  - [Authenticated Password Change](#authenticated-password-change)
  - [Get Current User (`/me`) & Swagger](#get-current-user-me--swagger)
- [HTTP API Reference](#http-api-reference)
- [Security Configuration (Swagger excluded)](#security-configuration-swagger-excluded)
- [Running Locally](#running-locally)
- [Configuration Reference](#configuration-reference)
- [Implementing SMS Delivery (`SmsSender`)](#implementing-sms-delivery-smssender)
- [End-to-end Examples (curl)](#end-to-end-examples-curl)
- [Security Notes & Best Practices](#security-notes--best-practices)
- [License](#license)
- [Contributing](#contributing)

---

## Why PULSE?

- **Phone-first onboarding**: Users register with a phone number and confirm identity via **OTP** (6 digits).  
- **Stateless security**: After verification, **JWT** secures API access; no server sessions required.  
- **Mongo TTL**: OTPs are **short-lived** and auto-expire using MongoDB TTL indexes.  
- **Provider-agnostic SMS**: You integrate your own SMS provider by implementing a tiny `SmsSender` interface.  
- **Drop-in module**: Clean package layout, DTOs, services, and controllers ready to reuse or extend.  

---

## Architecture & Tech Stack

**Language**: Java 21  
**Framework**: Spring Boot **3.5.5** (Web, Security, Validation)  
**Persistence**: Spring Data MongoDB  
**Auth**: JWT (HS256) via `io.jsonwebtoken:jjwt-* (0.11.5)`  
**Docs**: Swagger UI via `org.springdoc:springdoc-openapi-starter-webmvc-ui`  

**Package layout**
```
org.cheetah.pulse
├─ config/            # Security, Password encoder, CORS, OpenAPI config
├─ domain/            # Mongo documents: UserAccount, OtpToken, PasswordResetToken
├─ dto/               # Request/Response DTOs
├─ repo/              # Spring Data repositories
├─ security/          # JwtService, JwtAuthenticationFilter
├─ service/           # AuthService, OtpService, PasswordResetService, PasswordChangeService, SmsSender
└─ web/               # REST controllers (Auth, PasswordReset, Profile, User)
```

---

## Data Model

**UserAccount**
```
@Id private String id;
@Indexed(unique = true) private String phone;  // E.164 (+39...)
private String name;
private String surname;
private String passwordHash;
private LocalDate birthDate;
private boolean enabled;
private String email;
private Instant createdAt;
private Instant verifiedAt;
```

**OtpToken** (registration OTP) / **PasswordResetToken** (reset flow)
```
@Id private String id;
@Indexed private String phone;
private String code;     // 6 digits
private int attempts;
@Indexed(expireAfterSeconds = 0) private Instant expiresAt; // TTL index
```

Mongo’s TTL monitor runs roughly once per minute. Expiration is near-real-time, not exact to the second.

---

## Authentication Flows

### Registration with OTP
1. **POST `/auth/register`** → create user (disabled), generate **OTP**, send via SMS.  
2. **POST `/auth/verify`** → validate OTP (one-shot), set `enabled=true`.  
3. **POST `/auth/login`** → receive **JWT** for authenticated access.  

**Resend**: `/auth/resend?phone=...` (add rate-limits in production).

### Login (JWT)
`POST /auth/login` with `phone` + `password` returns **JWT** (HS256, `sub = userId`).  
`JwtAuthenticationFilter` authenticates requests via `Authorization: Bearer <token>`.

### Password Reset (OTP → short-lived reset token)
1. **POST `/auth/password/forgot`** → SMS a **PIN** (OTP).  
2. **POST `/auth/password/verify`** → return **resetToken** (JWT, short TTL, claim `typ=pwd_reset`).  
3. **POST `/auth/password/reset`** → set new password using `resetToken`.  

### Authenticated Password Change
**PATCH `/me/password`** → set a new password (no old password required, by design).  

Optional hardening: persist `passwordChangedAt`, then reject JWT with `iat < passwordChangedAt`.

### Get Current User (`/me`) & Swagger
- **GET `/me`** returns the authenticated user profile.  
- **Swagger UI** is enabled and **excluded from authentication** for convenience.

---

## HTTP API Reference

All endpoints use JSON for request/response.

### Registration & Login
**POST `/auth/register`**
```json
{ "phone": "+393401234567", "password": "MyPassw0rd!", "confirmPassword": "MyPassw0rd!" }
```
**200 OK**
```json
{ "message": "OTP sent via SMS", "token": null }
```

**POST `/auth/verify`**
```json
{ "phone": "+393401234567", "code": "123456" }
```
**200 OK**
```json
{ "message": "Registration confirmed", "token": null }
```

**POST `/auth/login`**
```json
{ "phone": "+393401234567", "password": "MyPassw0rd!" }
```
**200 OK**
```json
{ "message": "Login ok", "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

**POST `/auth/resend?phone=+393401234567`**  
**200 OK**
```json
{ "message": "New OTP sent", "token": null }
```

### Password Reset
**POST `/auth/password/forgot`**
```json
{ "phone": "+393401234567" }
```

**POST `/auth/password/verify`**
```json
{ "phone": "+393401234567", "code": "654321" }
```
**200 OK**
```json
{ "message": "Code verified", "resetToken": "eyJhbGciOi..." }
```

**POST `/auth/password/reset`**
```json
{ "resetToken": "eyJhbGciOi...", "newPassword": "NewPassw0rd!", "confirmPassword": "NewPassw0rd!" }
```
**200 OK**
```json
{ "message": "Password updated", "token": null }
```

### Authenticated Password Change
**PATCH `/me/password`** (Bearer JWT required)
```json
{ "newPassword": "AnotherPassw0rd!", "confirmPassword": "AnotherPassw0rd!" }
```
**200 OK**
```json
{ "message": "Password updated", "token": null }
```

### Current User
**GET `/me`** (Bearer JWT required)  
**200 OK** (example)
```json
{
  "id": "665f0c5c9b2a4a3e8c1d1234",
  "phone": "+393401234567",
  "name": "Mario",
  "surname": "Rossi",
  "email": "mario.rossi@example.com",
  "birthDate": "1985-02-10",
  "enabled": true,
  "createdAt": "2025-08-20T09:12:33Z",
  "verifiedAt": "2025-08-20T09:15:10Z"
}
```

---

## Security Configuration (Swagger excluded)

- Public endpoints: `/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/swagger-resources/**`, `/webjars/**`, `/actuator/health`  
- Everything else requires **JWT**.  
- **HTTP Basic** and **form login** are **disabled**; 401/403 return JSON.  
- OpenAPI configured with **Bearer** scheme; use the **Authorize** button in Swagger UI.  

Example:
```
.requestMatchers(
  "/actuator/health",
  "/auth/register", "/auth/resend", "/auth/verify", "/auth/login",
  "/auth/password/forgot", "/auth/password/verify", "/auth/password/reset",
  "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
  "/swagger-resources/**", "/webjars/**"
).permitAll()
.anyRequest().authenticated();
```

---

## Running Locally

### Requirements
- Java 21  
- Maven  
- MongoDB at `mongodb://localhost:27017/authdb`

### Configuration
`src/main/resources/application.yml`:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/authdb

app:
  otp:
    length: 6
    ttl-seconds: 300
    max-attempts: 5
  security:
    jwt:
      secret: "change-me-to-a-very-long-random-string-at-least-32-bytes"
      expiration-seconds: 3600
      reset-expiration-seconds: 600
      issuer: "auth-service"
```

Set via environment variable:
```bash
export APP_SECURITY_JWT_SECRET="<your-32+-byte-secret>"
```

### Build & Run
```bash
mvn clean package
java -jar target/pulse-auth-1.0.0-SNAPSHOT.jar
# or during development
mvn spring-boot:run
```

### Swagger
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Configuration Reference

- `app.otp.length` — number of digits (default **6**)  
- `app.otp.ttl-seconds` — OTP lifetime (default **300**)  
- `app.otp.max-attempts` — max attempts (default **5**)  
- `app.security.jwt.secret` — ≥ 32 bytes secret for HS256  
- `app.security.jwt.expiration-seconds` — access token TTL (default **3600**)  
- `app.security.jwt.reset-expiration-seconds` — reset token TTL (default **600**)  
- `app.security.jwt.issuer` — token issuer  
- `app.sms.provider` — conditional `SmsSender` beans  

---

## Implementing SMS Delivery (`SmsSender`)

**Important:** OTP delivery is **not bundled**. You must implement your own SMS integration.

**Interface**
```java
public interface SmsSender {
  void send(String phone, String message);
}
```

**Development logger**
```java
@Slf4j
@Service
public class LogSmsSender implements SmsSender {
  @Override public void send(String phone, String message) {
    log.info("SMS to {} -> {}", phone, message);
  }
}
```

**Provider example**
```java
@Service
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "twilio")
public class TwilioSmsSender implements SmsSender {
  @Override public void send(String phone, String message) {
    // TODO: invoke Twilio API
  }
}
```

Activate with:
```yaml
app:
  sms:
    provider: twilio
```

---

## End-to-end Examples (curl)

### Registration → Verify → Login
```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"phone":"+393401234567","password":"MyPassw0rd!","confirmPassword":"MyPassw0rd!"}'

# Verify OTP
curl -X POST http://localhost:8080/auth/verify \
  -H 'Content-Type: application/json' \
  -d '{"phone":"+393401234567","code":"123456"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"phone":"+393401234567","password":"MyPassw0rd!"}'
```

### Password Reset
```bash
curl -X POST http://localhost:8080/auth/password/forgot \
  -H 'Content-Type: application/json' \
  -d '{"phone":"+393401234567"}'

curl -X POST http://localhost:8080/auth/password/verify \
  -H 'Content-Type: application/json' \
  -d '{"phone":"+393401234567","code":"654321"}'

curl -X POST http://localhost:8080/auth/password/reset \
  -H 'Content-Type: application/json' \
  -d '{"resetToken":"<token>","newPassword":"NewPassw0rd!","confirmPassword":"NewPassw0rd!"}'
```

### Change Password & `/me`
```bash
# Change password
curl -X PATCH http://localhost:8080/me/password \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"newPassword":"AnotherPassw0rd!","confirmPassword":"AnotherPassw0rd!"}'

# Get current user
curl http://localhost:8080/me -H "Authorization: Bearer $TOKEN"
```

---

## Security Notes & Best Practices

- Keep OTP TTL **short** and enforce **attempt limits**.  
- **Do not log** OTP codes in production.  
- Use a **random secret ≥ 32 bytes** for JWT.  
- Consider **rate-limiting** auth endpoints.  
- Invalidate old tokens after password change via `passwordChangedAt`.  

---

## License

This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0-only)**.  
You may **use, modify, and distribute** under AGPL-3.0 terms.  

Full license text: [AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.txt)

---

## Contributing

Issues and PRs are welcome. Please include tests for new functionality and keep the public API stable.
