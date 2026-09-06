# SpringBootProjects (Brite Technology Notifications)

A Spring Boot 3 REST application demonstrating configuration properties binding (`@ConfigurationProperties`), custom REST controllers, global exception handling, Spring Data JPA, and Spring Boot Actuator monitoring.

---

## 🚀 Features

- **Configuration Management**: Strongly-typed properties bound via `@ConfigurationProperties` for notification options (App, Email, SMS, Retry).
- **REST Endpoints**: Exposes endpoints under `/v1/orders` to query live application, email, and SMS configurations.
- **Product Catalog API**: Exposes endpoints under `/v1/product` to list, look up, and add products (in-memory catalog).
- **Actuator Monitoring**: Integrated Spring Boot Actuator exposing health status under `/actuator/health`.
- **Global Exception Handling**: Centralized exception handling using `@ControllerAdvice`.
- **Database Integration**: MySQL datasource integration with Hibernate / Spring Data JPA.

---

## 🛠️ Prerequisites & Technology Stack

- **Java**: 21
- **Spring Boot**: 3.4.3
- **Build Tool**: Maven 3.9+
- **Lombok**: 1.18.48
- **Database**: MySQL 8.x

---

## ⚙️ Configuration Properties (`application.yml`)

The application runs on port **`8081`** with a base servlet context path **`/brite/api`**.

```yaml
server:
  port: 8081
  servlet:
    context-path: /brite/api

management:
  endpoints:
    web:
      base-path: /actuator

spring:
  profiles:
    active: dev
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db_example?useSSL=false
    username: root
    password: <password>
```

---

## 🌐 API Endpoints

All REST endpoints are prefixed with `http://localhost:8081/brite/api`:

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `GET` | `/v1/orders/app-config/values` | Returns application connection pool size and timeout settings |
| `GET` | `/v1/orders/email-config/values` | Returns email notification configuration values |
| `GET` | `/v1/orders/sms-confi/values` | Returns SMS notification configuration values |
| `GET` | `/v1/product/allproducts` | Returns all products in the catalog |
| `GET` | `/v1/product/productId/{productId}` | Returns a single product by ID, or `404` if not found |
| `POST` | `/v1/product/addproduct` | Adds a new product to the catalog and returns it |
| `GET` | `/v1/product/productmessage` | Triggers an internal product/user lookup and returns a confirmation message |
| `GET` | `/actuator/health` | Returns Spring Boot Actuator application health status |

---

## 🧪 Building & Running

### 1. Compile the Project
```bash
mvn clean compile
```

### 2. Run Tests
```bash
mvn test
```

### 3. Start the Server
```bash
mvn spring-boot:run
```

---

## 🔍 Sample cURL Requests

```bash
# Get Application Config
curl -s http://localhost:8081/brite/api/v1/orders/app-config/values

# Get Email Config
curl -s http://localhost:8081/brite/api/v1/orders/email-config/values

# Get SMS Config
curl -s http://localhost:8081/brite/api/v1/orders/sms-confi/values

# Check Actuator Health
curl -s http://localhost:8081/brite/api/actuator/health

# Get All Products
curl -s http://localhost:8081/brite/api/v1/product/allproducts

# Get Product By ID
curl -s http://localhost:8081/brite/api/v1/product/productId/101

# Add a Product
curl -s -X POST http://localhost:8081/brite/api/v1/product/addproduct \
  -H "Content-Type: application/json" \
  -d '{"productId":"200","productName":"Test Widget","quantity":"5","price":42.5}'
```

Sample JSON Response (`/app-config/values`):
```json
{
  "connectionPoolSize": "10",
  "timeoutInSeconds": "1"
}
```

---

## ✅ Test Suite

Tests use `@SpringBootTest` + `MockMvc` and require a running MySQL instance (same as the app itself). Run with:
```bash
mvn test
```

| Test Class | Covers |
| :--- | :--- |
| `ProductControllerTest` | `/v1/product` endpoints — list, get by ID (found + `404` not-found), add, product message |
| `BriteOrdersViewConfigValuesTest` | `/v1/orders` config endpoints — app, email, SMS |
| `SpringBootProjectsApplicationTests` | Application context load + actuator health |
