# SpringBootProjects (Brite Technology Notifications)

A Spring Boot 3 REST application demonstrating configuration properties binding (`@ConfigurationProperties`), custom REST controllers, async processing, JSON Schema-validated event ingestion, global exception handling, Spring Data JPA, and Spring Boot Actuator monitoring.

---

## 🚀 Features

- **Configuration Management**: Strongly-typed properties bound via `@ConfigurationProperties` for notification options (App, Email, SMS, Retry).
- **Configs API**: Exposes endpoints under `/v1/configs` to query live application, email, and SMS configurations.
- **Product Catalog API**: Exposes endpoints under `/v1/product` to list, look up, and add products (in-memory catalog).
- **Banking APIs**: Client/account lookup, registration, withdrawal, and deposit (`/v1/client`, `/v1/api/accounts`) — withdrawals and deposits are applied atomically per account and withdrawals are recorded to an in-memory history — plus async notification demos (`/notify`, `/report`) backed by `@Async`. Account numbers are always `CH-`/`SV-` (checking/savings) followed by a zero-padded 10-digit number (e.g. `CH-0000088291`), whether seeded or generated on registration. 52 demo accounts (26 checking, 26 savings) are seeded on startup.
- **Banking API Gateway & Rate Limiter**: Every banking endpoint (`/v1/api/accounts/**`, `/v1/client/**`, `/v1/payment/**`, `/notify`, `/notify-sms`, `/report`) sits behind a `Filter`-based gateway ingress layer that requires an `X-Customer-Id` header and caps each customer to a configurable number of requests per day (`banking.rate-limit`, default 1000/day) — see [Banking API gateway](#-banking-api-gateway--rate-limiter) below. Retail/events/configs endpoints are unaffected.
- **Account Constraints**: Configurable business rules (`banking.constraints`) enforced on registration/withdrawal/deposit — minimum age to open an account, minimum balance retained after a withdrawal (checking/savings), and a maximum single cash-deposit amount.
- **Bank Statement**: `/v1/api/accounts/{accountNumber}/statement` returns an account's deposit/withdrawal history for a given date range, capped by a configurable maximum range in months.
- **Resilience Demo**: `/v1/payment/process` demonstrates a Resilience4j circuit breaker with jittered exponential-backoff retry around a simulated flaky downstream call.
- **Event Ingestion**: `/api/events` accepts versioned event payloads validated against a JSON Schema (`event-v1.json`) and persisted via Spring Data JPA.
- **API Versioning Demo**: `/apiversion` illustrates URI-, query-param-, header-, and content-negotiation-based API versioning strategies.
- **Actuator Monitoring**: Integrated Spring Boot Actuator exposing health status under `/actuator/health`.
- **Endpoint Execution-Time Logging**: A Spring AOP `@Aspect` (`ExecutionTimeLoggingAspect`, `org.bee.common.logging`) wraps every `@RestController` method app-wide and logs its execution time in milliseconds — no code changes needed per controller.
- **Account Search Caching**: `GET /v1/api/accounts` results are cached for 10 minutes via Spring's `@Cacheable` (Caffeine, `spring.cache.caffeine.spec: expireAfterWrite=10m`), evicted whenever an account is registered or closed.
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
  endpoint:
    health:
      probes:
        enabled: true
      show-details: always
      group:
        readiness:
          include: readinessState,db

spring:
  profiles:
    active: dev
  cache:
    type: caffeine
    cache-names: accountSearch
    caffeine:
      spec: expireAfterWrite=10m
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
  rate-limit:
    enabled: true
    requests-per-day: 1000
    customer-header-name: X-Customer-Id

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

> Every endpoint below requires an `X-Customer-Id` header and is subject to the per-customer daily rate limit — see [Banking API gateway & rate limiter](#banking-api-gateway--rate-limiter).

| Method | Endpoint Path | Description |
| :--- | :--- | :--- |
| `GET` | `/v1/client/name` | Returns a sample customer record |
| `GET` | `/v1/api/accounts?accountNumber=&status=&createdFrom=&createdTo=&closedFrom=&closedTo=&months=&page=&size=&sort=` | Retrieves account ids/details within a createdDate/closedDate range, paginated. All filters optional. **Default lookback**: if neither `createdFrom` nor `createdTo` is given, defaults to "as of today minus `months` months" (18 months if `months` is also omitted); supplying either explicit created-date bound disables this default and `months` is ignored (`400` if `months` isn't positive). **Conditional lookup**: if `accountNumber` is given, only that account is returned (still subject to the other filters — an out-of-range match yields an empty page, not a bypass); if omitted/null, every matching account is returned. Other filters: `status` (`ACTIVE`/`CLOSED`), `closedFrom`/`closedTo` (inclusive `yyyy-MM-dd` range), standard Spring Data `page`/`size`/`sort` (sortable by `createdDate`, `closedDate`, `accountStatus`, `checkingAccountNumber`, `savingAccountNumber`; default `size=20`, sorted by `createdDate` ascending). Returns a Spring Data `Page<AccountStatusView>` envelope (`content`, `totalElements`, `totalPages`, etc) — each row is flattened to `accountNumber`, `accountType`, `accountStatus`, `createdDate`, `closedDate`, `firstName`, `lastName`, not the full nested `Account`/`Customer`. `400` if a `*From` date is after its `*To` date or an unsupported `sort` property is given |
| `POST` | `/v1/api/accounts/lookup` | Looks up an account by account number; `404` if not found |
| `POST` | `/v1/api/accounts/register` | Registers a new customer + account |
| `POST` | `/v1/api/accounts/withdraw` | Withdraws funds from a checking/savings account; `400` on insufficient funds, mismatched account type, or a `CLOSED` account, `404` if the account doesn't exist |
| `POST` | `/v1/api/accounts/deposit` | Deposits funds into a checking/savings account; `400` on invalid amount/deposit type, mismatched account type, a cash amount over the configured maximum, or a `CLOSED` account, `404` if the account doesn't exist |
| `POST` | `/v1/api/accounts/{accountNumber}/close` | Closes a checking/savings account (status `ACTIVE` → `CLOSED`, stamps `closedDate`); `400` if already closed, `404` if the account doesn't exist |
| `GET` | `/v1/api/accounts/{accountNumber}/statement?beginDate=yyyy-MM-dd&endDate=yyyy-MM-dd` | Returns a bank statement (deposit/withdrawal history) for the account in the given range; `400` if the range exceeds the configured maximum months, `404` if the account doesn't exist |
| `GET` | `/notify?name={name}` | Fire-and-forget async email notification demo |
| `GET` | `/report` | Async task that returns a completed report string |

### Banking API gateway & rate limiter

`BankingRateLimitFilter` (`org.bee.banking.gateway`) is a servlet `Filter` registered only for the banking module's URL patterns (`/v1/api/accounts/*`, `/v1/client/*`, `/v1/payment/*`, `/notify`, `/notify-sms`, `/report`) — it runs before `DispatcherServlet`, so rejected requests never reach a controller. It acts as a lightweight API-gateway ingress layer with two responsibilities:

1. **Customer identification** — every request must carry the header configured by `banking.rate-limit.customer-header-name` (default `X-Customer-Id`). Missing/blank header → `400` with a plain-text explanation.
2. **Per-customer daily rate limit** — each customer ID is capped at `banking.rate-limit.requests-per-day` (default **1000**) requests per calendar day, tracked in-memory and reset at midnight. Exceeding it → `429 Too Many Requests`.

Every response that reaches the filter (allowed or rejected) carries `X-RateLimit-Limit` and `X-RateLimit-Remaining` headers. Set `banking.rate-limit.enabled: false` to bypass the whole gateway (e.g. for local scripting). This is a single-instance, in-memory limiter (like the rest of the app's mock stores) — not a distributed rate limiter — and the customer ID is a caller-supplied header rather than an authenticated principal, since the app has no auth layer.

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
| `GET` | `/actuator/health` | Returns Spring Boot Actuator application health status (aggregates the `liveness`/`readiness` groups plus `db`, disk space, etc.) |
| `GET` | `/actuator/health/liveness` | Kubernetes liveness probe — `UP` as long as the process is running; never reflects the MySQL connection |
| `GET` | `/actuator/health/readiness` | Kubernetes readiness probe — `UP` only while the app's readiness state is `ACCEPTING_TRAFFIC` **and** the MySQL `db` health indicator is `UP` |

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

# Kubernetes-style liveness/readiness probes
curl -s http://localhost:8081/brite/actuator/health/liveness
curl -s http://localhost:8081/brite/actuator/health/readiness

# Get All Products
curl -s http://localhost:8081/brite/v1/product/allproducts

# Get Product By ID
curl -s http://localhost:8081/brite/v1/product/productId/101

# Add a Product
curl -s -X POST http://localhost:8081/brite/v1/product/addproduct \
  -H "Content-Type: application/json" \
  -d '{"productId":"200","productName":"Test Widget","quantity":"5","price":42.5}'

# List/Search Accounts (paginated; all filters optional). The two seeded CLOSED
# accounts are ~24-30 months old, so months must be widened to see them.
curl -s -H "X-Customer-Id: demo-customer-1" \
  "http://localhost:8081/brite/v1/api/accounts?status=CLOSED&months=36&page=0&size=10&sort=createdDate,desc"

# Same endpoint, narrowed to one account (still checked against the resolved date range)
curl -s -H "X-Customer-Id: demo-customer-1" \
  "http://localhost:8081/brite/v1/api/accounts?accountNumber=CH-0000088291&months=6"

# No explicit dates: defaults to accounts created in the last 18 months (as of today)
curl -s -H "X-Customer-Id: demo-customer-1" "http://localhost:8081/brite/v1/api/accounts"

# Override the default lookback window (last 6 months instead of 18)
curl -s -H "X-Customer-Id: demo-customer-1" "http://localhost:8081/brite/v1/api/accounts?months=6"

# Look Up a Client Account (banking endpoints require X-Customer-Id, rate-limited to 1000/day)
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/lookup \
  -H "Content-Type: application/json" -H "X-Customer-Id: demo-customer-1" \
  -d '{"accountNumber":"CH-0000088291"}'

# Register a New Client Account
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/register \
  -H "Content-Type: application/json" -H "X-Customer-Id: demo-customer-1" \
  -d '{
        "firstName":"David","lastName":"Miller","dateOfBirth":"08/19/1994",
        "street":"789 Pine Rd","city":"Houston","state":"TX","zip":"77001",
        "accountType":"CHECKING"
      }'

# Withdraw From a Client Account
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/withdraw \
  -H "Content-Type: application/json" -H "X-Customer-Id: demo-customer-1" \
  -d '{
        "accountNumber":"CH-0000088291","accountType":"CHECKING","withdrawAmount":100.00,
        "firstName":"Alice","lastName":"Smith","street":"123 Main St","city":"Austin",
        "state":"TX","zip":"78701","addressLine1":"Apt 4B"
      }'

# Deposit Into a Client Account
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/deposit \
  -H "Content-Type: application/json" -H "X-Customer-Id: demo-customer-1" \
  -d '{
        "accountNumber":"CH-0000088291","amount":250.00,"accountType":"CHECKING","depositType":"cash",
        "firstName":"Alice","lastName":"Smith","street":"123 Main St","city":"Austin",
        "state":"TX","zip":"78701","addressLine1":"Apt 4B"
      }'

# Close a Client Account
curl -s -X POST http://localhost:8081/brite/v1/api/accounts/CH-0000088291/close \
  -H "X-Customer-Id: demo-customer-1"

# Get a Bank Statement
curl -s "http://localhost:8081/brite/v1/api/accounts/CH-0000088291/statement?beginDate=2026-01-01&endDate=2026-12-31" \
  -H "X-Customer-Id: demo-customer-1"

# Trigger a Fire-and-Forget Async Notification
curl -s "http://localhost:8081/brite/notify?name=Alice" -H "X-Customer-Id: demo-customer-1"

# Call the Resilience4j Circuit Breaker + Retry Demo (run a few times to see variation)
curl -s -X POST http://localhost:8081/brite/v1/payment/process -H "X-Customer-Id: demo-customer-1"

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
| `SpringBootProjectsApplicationTests` | Application context load + actuator health, liveness, and readiness probes |
| `AccountRepositoryTest` | Plain unit test (no Spring context/MySQL) — account creation defaults, ACTIVE/CLOSED status lifecycle, withdraw/deposit balance rules, account search/pagination/sorting/date-range filters, conditional accountNumber filter |
| `ClientAccountServiceTest` | Plain unit test (no Spring context/MySQL) — registration age gating, withdraw/deposit input validation |
| `AccountStatusStatementServiceTest` | Plain unit test (no Spring context/MySQL) — account search date-range validation, Account → AccountStatusView mapping (checking vs savings account number, customer name), conditional accountNumber pass-through, default/overridden `months` lookback window |
| `CustomerRateLimiterTest` | Plain unit test (no Spring context/MySQL) — per-customer daily counter: decrements, blocks past the limit, independent per customer |
| `BankingRateLimitFilterTest` | Plain unit test (no Spring context/MySQL) — missing-header rejection, within-limit pass-through + headers, over-limit `429` |
| `ExecutionTimeLoggingAspectTest` | Plain unit test (no Spring context/MySQL) — the `@Around` advice returns the join point's result and propagates exceptions unchanged |
| `AccountSearchCachingTest` | Plain unit test (no Spring context/MySQL) — reflection check that `listAccountStatuses` carries `@Cacheable` and `registerNewClientAccount`/`closeAccount` carry the matching `@CacheEvict` |
