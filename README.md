## Branch Strategy

- `master`  
  Synchronous authentication service (Spring MVC + JPA)

- `reactive`  
  Reactive authentication service (Spring WebFlux + Reactive Repositories)

Choose the branch based on your project architecture.

---

# 🔐 Authentication & Authorization API – Spring Boot

A secure **Authentication and Authorization REST API** built using **Spring Boot**, **Spring Security**, **JWT**, and **H2/MySQL**.  
This project demonstrates real-world authentication flows including **user registration, login, role-based access control**, and **JWT-based security**.

---

## 🚀 Features

- User Login with Username & Password
- JWT-based Authentication
- Refresh Token Mechanism
- Role-based Authorization (USER / ADMIN)
- OAuth2 Login Support
- Secure HTTP-only Cookie Handling
- Global Exception Handling
- Centralized Logging using AOP
- Swagger / OpenAPI Documentation
- Spring Security Best Practices
---

## 🛠 Tech Stack

- **Java  21**
- **Spring Boot**
- **Spring Security**
- **JWT (JSON Web Token)**
- **OAuth2**
- **Spring Data JPA**
- **H2 Database**
- **Maven**
- **Swagger (OpenAPI 3)**
- **Docker & Docker Compose**
- **JUnit 5 / MockMvc**
- 
##  📁 Project Structure

```
src
├── main
│   ├── java
│   │   └── com.koyta.auth
│   │       |
│   │       ├── aspect
│   │       │   └── LoggingAspect.java
│   │       |
│   │       ├── config
│   │       │   ├── ProjectConfig.java
│   │       │   ├── SecurityConfig.java
│   │       │   └── SwaggerConfig.java
│   │       |
│   │       ├── controller
│   │       │   └── AuthController.java
│   │       |
│   │       ├── contract               
│   │       │   ├── JsonContractValidator.java
│   │       │   
│   │       |
│   │       ├── dto
│   │       │   ├── LoginRequest.java
│   │       │   ├── RefreshTokenRequest.java
│   │       │   ├── TokenResponse.java
│   │       │   ├── UserDto.java
│   │       │   └── RoleDto.java
│   │
│   │       ├── entity
│   │       │   ├── User.java
│   │       │   ├── Role.java
│   │       │   ├── Provider.java
│   │       │   └── RefreshToken.java
│   │
│   │       ├── exception
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── ContractValidationException.java 
│   │       │   ├── AuthenticationFailedException.java
│   │       │   ├── JwtTokenExpiredException.java
│   │       │   ├── ResourceNotFoundException.java
│   │       │   └── ExistDataException.java
│   │       |
│   │       ├── filter                 
│   │       │   ├── ContractValidationFilter.java
│   │       │   └── wrapper
│   │       │       └── CachedBodyHttpServletRequest.java
│   │       |
│   │       ├── repository
│   │       │   ├── UserRepository.java
│   │       │   └── RefreshTokenRepository.java
│   │       |
│   │       ├── security
│   │       │   ├── JwtAuthenticationFilter.java
│   │       │   ├── JwtService.java
│   │       │   ├── JwtServiceImpl.java
│   │       │   ├── CustomUserDetails.java
│   │       │   ├── UserDetailsServiceImpl.java
│   │       │   └── OAuth2SuccessHandler.java
│   │       |
│   │       ├── service
│   │       │   ├── AuthService.java
│   │       │   ├── AuthServiceImpl.java
│   │       │   ├── UserService.java
│   │       │   └── UserServiceImpl.java
│   │       |
│   │       └── util
│   │           ├── CookieService.java
│   │           ├── UserHelper.java
│   │           └── AppConstants.java
│   |
│   └── resources
│       ├── application.yml
│       ├── application-dev.yml
│       └── logback.xml
│
└── test
    └── java
        └── com.koyta.auth
            ├── contract
            ├── filter
            ├── security
            ├── service
            ├── controller
            ├── util
            └── integration

```

### 📦 Package Overview
- **controller** – REST API endpoints
- **service** – Business logic
- **repository** – Database interaction
- **security** – JWT, OAuth2 & Spring Security
- **config** – Security, Swagger & app configs
- **aspect** – Logging using AOP
- **exception** – Centralized error handling
- **dto** – Request & response models
- **util** – Helper & utility classes

---

## 🔑 Authentication Flow

1. User logs in using /api/auth/login
2. Credentials are validated
3. Server generates:
- Access Token (JWT)
- Refresh Token
4. Access token is used for secured APIs
5. Refresh token is stored securely (HTTP-only cookie / DB)
6. Client requests new access token using refresh token

Logout invalidates refresh token
---

## 📌 API Endpoints

### 🔐 Auth APIs

| Method | Endpoint            | Description                         |
| ------ | ------------------- | ----------------------------------- |
| POST   | `/api/auth/login`   | Authenticate user & generate tokens |
| POST   | `/api/auth/refresh` | Generate new access token           |
| POST   | `/api/auth/logout`  | Invalidate refresh token            |

---

### 👤 User APIs

| Method | Endpoint        | Role |
| ------ | --------------- | ---- |
| GET    | `/api/users/me` | USER |


---

## 🔑 Security Implementation

- JWT validation via JwtAuthenticationFilter
- User authentication via CustomUserDetails
- OAuth2 login handled using OAuth2SuccessHandler
- Role-based authorization using Spring Security
- Passwords encrypted using BCrypt

---

## 📜 Contract Validation

This project includes a **Contract Validation Module** to ensure request payload integrity.

### ✅ Features
- JSON request validation before controller execution
- Centralized contract validation using filter
- Custom `ContractValidationException`
- Integrated with GlobalExceptionHandler

### 🔄 Flow
1. Incoming request intercepted via `ContractValidationFilter`
2. Request body validated using `JsonContractValidator`
3. If invalid → throws `ContractValidationException`
4. Handled globally → returns structured error response

### 📁 Location
- com.koyta.auth.contract

---

## 🍪 Refresh Token & Cookie Handling

- Refresh tokens stored in database
- Sent using HTTP-only secure cookies
- Managed via CookieService
- Token rotation supported

---

## Exception Handling

Centralized exception handling using @RestControllerAdvice

| HTTP Code | Description           |
| --------- | --------------------- |
| 400       | Bad Request           |
| 401       | Unauthorized          |
| 403       | Forbidden             |
| 404       | Resource Not Found    |
| 409       | Conflict              |
| 500       | Internal Server Error |

---

## Exception Handling

- Access Swagger UI after starting application:

   http://localhost:8080/swagger-ui.html
---


## Running the Application

```bash
  mvn clean install
  mvn spring-boot:run
```
---


## 🔐 Environment Variables

- JWT_SECRET=your_jwt_secret
- JWT_EXPIRATION=3600000
- REFRESH_TOKEN_EXPIRATION=604800000
----

## Logging & Monitoring

- Method-level logging using AOP
- Centralized logs via LoggingAspect
- Configurable via logback.xml
- 
---

 ## 🗄 H2 Database

- H2 is used for testing

- H2 Console enabled

- Access H2 Console
http://localhost:8080/h2-console


- JDBC URL
- jdbc:h2:file:/data/testdb
----

## 🧪 Testing & Coverage

This project includes comprehensive test coverage:

### ✔ Test Types
- Unit Tests (Service, Utility, Security)
- Integration Tests (Controllers)
- Filter & Aspect Testing
- Contract Validation Tests

### ✔ Tools Used
- JUnit 5
- Mockito
- MockMvc
- JaCoCo

### ✔ Coverage Focus
- Branch Coverage (Primary focus)
- Edge Case Handling
- Exception Flow Coverage

---

## 📊 Test Coverage

The project maintains high test coverage using JaCoCo.

### 📌 Coverage Summary

- **Classes:** 97.3%
- **Methods:** 94.5%+
- **Lines:** 98.2%+
- **Branches:** 99%+

### 📷 Coverage Report
<img width="1920" height="963" alt="Screenshot (37)" src="https://github.com/user-attachments/assets/907e75d0-01eb-4516-9cb2-fd4198511ee6" />

---

## 🐳 Docker Image Optimization
This project includes a highly optimized Docker setup to reduce image size and improve performance.

### ⚙️ Optimization Strategy
- Multi-stage Docker build (build + runtime separation)
- Custom Java Runtime using jlink
- Removed unnecessary dependencies:
        - Swagger (OpenAPI UI)
        - Spring Boot DevTools
- Optimized Maven dependency caching
- Reduced Docker layers
---

### Results
### 📉 Results

| Metric            | Before  | After   |
| ----------------- | ------- | ------- |
| Docker Image Size | ~700 MB | ~250 MB |

---

### 🧠 Why Optimization Matters
- ⚡ Faster deployment
- 💰 Lower cloud cost
- 🔐 Better security (reduced attack surface)
- 📦 Efficient container distribution
---

## ✅ Highlights

- 100% Unit Test Coverage for critical services
- Strong Branch Coverage using edge case testing
- Production-ready exception handling
- Secure authentication using JWT & OAuth2
--

## 🧑‍💻 Author

**Abhishek Narkhede**  
🚀 Java Backend Developer

**Tech Stack:** Spring Boot • Spring Security • JWT • OAuth2 • Microservices
