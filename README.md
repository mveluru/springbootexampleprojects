# SpringBootProjects (Brite Technology Notifications)

A Spring Boot 3 REST application demonstrating configuration properties binding (`@ConfigurationProperties`), custom REST controllers, async processing, JSON Schema-validated event ingestion, global exception handling, Spring Data JPA, and Spring Boot Actuator monitoring.

---

## 🚀 Features

- **Configuration Management**: Strongly-typed properties bound via `@ConfigurationProperties` for notification options (App, Email, SMS, Retry).
- **Configs API**: Exposes endpoints under `/v1/configs` to query live application, email, and SMS configurations.
- **Product Catalog API**: Exposes endpoints under `/v1/product` to list, look up, and add products (in-memory catalog).
- **Banking APIs**: Client/account lookup, registration, withdrawal, and deposit (`/v1/client`, `/v1/api/accounts`) — withdrawals and deposits are applied atomically per account and withdrawals are recorded to an in-memory history — plus async notification demos (`/notify`, `/report`) backed by `@Async`.
- **Account Constraints**: Configurable business rules (`banking.constraints`) enforced on registration/withdrawal/deposit — minimum age to open an account, minimum balance retained after a withdrawal (checking/savings), and a maximum single cash-deposit amount.
- **Bank Statement**: `/v1/api/accounts/{accountNumber}/statement` returns an account's deposit/withdrawal history for a given date range, capped by a configurable maximum range in months.
- **Resilience Demo**: `/v1/payment/process` demonstrates a Resilience4j circuit breaker with jittered exponential-backoff retry around a simulated flaky downstream call.
- **Event Ingestion**: `/api/events` accepts versioned event payloads validated against a JSON Schema (`event-v1.json`) and persisted via Spring Data JPA.
- **API Versioning Demo**: `/apiversion` illustrates URI-, query-param-, header-, and content-negotiation-based API versioning strategies.
- **Actuator Monitoring**: Integrated Spring Boot Actuator exposing health status under `/actuator/health`.
- **Global Exception Handling**: Centralized exception handling using `@ControllerAdvice`, with all exception and validation messages centralized in `BankingMessages`.
- **Structured Logging**: Slf4j logging across the banking module — info logs for successful operations, warn logs for validation/business rejections (insufficient funds, account not found, etc.) — with log message templates also centralized in `BankingMessages`.
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

The application runs on port **`8081`** with a base servlet context path **`/brite`**.

```yaml
server:
  port: 8081
  servlet:
    context-path: /brite

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

banking:
  constraints:
    maximum-deposit-amount-by-cash: 5000.00
    minimum-age: 18
    checking-minimum-balance: 25.00
    saving-minimum-balance: 100.00
    max-statement-range-months: 18

resilience4j:
  circuitbreaker:
    instances:
      bankService:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
        permittedNumberOfCallsInHalfOpenState: 3
  retry:
    instances:
      bankService:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        enableRandomizedWait: true   # adds jitter to the backoff
        randomizedWaitFactor: 0.5
```

---

## 🌐 API Endpoints

All REST endpoints are prefixed with `http://localhost:8081/brite`:

### Configs — `/v1/configs`

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `GET` | `/v1/configs/app-config/values` | Returns application connection pool size and timeout settings |
| `GET` | `/v1/configs/email-config/values` | Returns email notification configuration values |
| `GET` | `/v1/configs/sms-config/values` | Returns SMS notification configuration values |

### Product catalog — `/v1/product`

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `GET` | `/v1/product/allproducts` | Returns all products in the catalog |
| `GET` | `/v1/product/productId/{productId}` | Returns a single product by ID, or `404` if not found |
| `POST` | `/v1/product/addproduct` | Adds a new product to the catalog and returns it |
| `GET` | `/v1/product/productmessage` | Triggers an internal product/user lookup and returns a confirmation message |

### Banking — clients, accounts & notifications

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `GET` | `/v1/client/name` | Returns a sample customer record |
| `POST` | `/v1/api/accounts/lookup` | Looks up an account by account number; `404` if not found |
| `POST` | `/v1/api/accounts/register` | Registers a new customer + account |
| `POST` | `/v1/api/accounts/withdraw` | Withdraws funds from a checking/savings account; `400` on insufficient funds or mismatched account type, `404` if the account doesn't exist |
| `POST` | `/v1/api/accounts/deposit` | Deposits funds into a checking/savings account; `400` on invalid amount/deposit type, mismatched account type, or a cash amount over the configured maximum, `404` if the account doesn't exist |
| `GET` | `/v1/api/accounts/{accountNumber}/statement?beginDate=yyyy-MM-dd&endDate=yyyy-MM-dd` | Returns a bank statement (deposit/withdrawal history) for the account in the given range; `400` if the range exceeds the configured maximum months, `404` if the account doesn't exist |
| `GET` | `/notify?name={name}` | Fire-and-forget async email notification demo |
| `GET` | `/report` | Async task that returns a completed report string |

### Resilience demo — `/v1/payment`

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `POST` | `/v1/payment/process` | Calls a simulated flaky bank service (40% failure rate) through a Resilience4j circuit breaker + jittered exponential-backoff retry; returns a fallback message once the breaker opens |

### Event ingestion — `/api/events`

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `POST` | `/api/events` | Validates a `v1` event payload against JSON Schema, then persists it |

### Sample lookup — `/v1/sample`

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `GET` | `/v1/sample/spl?item={item}` | Looks up a sample item count by name (e.g. `Mac`, `Dell`, `IBM`) |

### API versioning demo — `/apiversion`

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `GET` | `/apiversion/v1/api` | Versioning via URI path (v1) |
| `GET` | `/apiversion/v2/api` | Versioning via URI path (v2) |
| `GET` | `/apiversion/api?v1` | Versioning via request parameter (v1) |
| `GET` | `/apiversion/api?v2` | Versioning via request parameter (v2) |
| `GET` | `/apiversion/api` (header `X-API-VERSION: 1`) | Versioning via custom header (v1) |
| `GET` | `/apiversion/api` (header `X-API-VERSION: 2`) | Versioning via custom header (v2) |
| `GET` | `/apiversion/api` (`Accept: application/vnd.company.app-v1+json`) | Versioning via content negotiation (v1) |
| `GET` | `/apiversion/api` (`Accept: application/vnd.company.app-v2+json`) | Versioning via content negotiation (v2) |

### Monitoring

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
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
curl -s http://localhost:8081/brite/v1/configs/app-config/values

# Get Email Config
curl -s http://localhost:8081/brite/v1/configs/email-config/values

# Get SMS Config
curl -s http://localhost:8081/brite/v1/configs/sms-config/values

# Check Actuator Health
curl -s http://localhost:8081/brite/actuator/health

# Get All Products
curl -s http://localhost:8081/brite/v1/product/allproducts

# Get Product By ID
curl -s http://localhost:8081/brite/v1/product/productId/101

# Add a Product
curl -s -X POST http://localhost:8081/brite/v1/product/addproduct \
  -H "Content-Type: application/json" \
  -d '{"productId":"200","productName":"Test Widget","quantity":"5","price":42.5}'

# Look Up a Client Account
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/lookup \
  -H "Content-Type: application/json" \
  -d '{"accountNumber":"CH-88291"}'

# Register a New Client Account
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/register \
  -H "Content-Type: application/json" \
  -d '{
        "firstName":"David","lastName":"Miller","dateOfBirth":"08/19/1994",
        "street":"789 Pine Rd","city":"Houston","state":"TX","zip":"77001",
        "accountType":"CHECKING"
      }'

# Withdraw From a Client Account
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/withdraw \
  -H "Content-Type: application/json" \
  -d '{
        "accountNumber":"CH-88291","accountType":"CHECKING","withdrawAmount":100.00,
        "firstName":"Alice","lastName":"Smith","street":"123 Main St","city":"Austin",
        "state":"TX","zip":"78701","addressLine1":"Apt 4B"
      }'

# Deposit Into a Client Account
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/deposit \
  -H "Content-Type: application/json" \
  -d '{
        "accountNumber":"CH-88291","amount":250.00,"accountType":"CHECKING","depositType":"cash",
        "firstName":"Alice","lastName":"Smith","street":"123 Main St","city":"Austin",
        "state":"TX","zip":"78701","addressLine1":"Apt 4B"
      }'

# Get a Bank Statement
curl -s "http://localhost:8081/brite/v1/api/accounts/CH-88291/statement?beginDate=2026-01-01&endDate=2026-12-31"

# Trigger a Fire-and-Forget Async Notification
curl -s "http://localhost:8081/brite/notify?name=Alice"

# Call the Resilience4j Circuit Breaker + Retry Demo (run a few times to see variation)
curl -s -X POST http://localhost:8081/brite/v1/payment/process

# Submit a v1 Event
curl -s -X POST http://localhost:8081/brite/api/events \
  -H "Content-Type: application/json" \
  -d '{
        "version":"v1","eventId":"evt_123abc","timestamp":"2026-09-17T10:00:00Z",
        "payload":{"userId":"u123","email":"user@example.com"}
      }'

# API Versioning via URI Path
curl -s http://localhost:8081/brite/apiversion/v1/api

# Sample Item Lookup
curl -s "http://localhost:8081/brite/v1/sample/spl?item=Mac"
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
| `BriteConfigValuesControllerTest` | `/v1/configs` config endpoints — app, email, SMS |
| `SpringBootProjectsApplicationTests` | Application context load + actuator health |
