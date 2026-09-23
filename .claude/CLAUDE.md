# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

A Spring Boot 3 (Java 21) REST application (`org.bee`, artifact `SBProjects`) that demonstrates several backend patterns in isolated modules: `@ConfigurationProperties` binding, async notification processing, JSON Schema-validated event ingestion, Resilience4j circuit breaking/retry, MapStruct mapping, and Spring Data JPA — see `../README.md` for the full endpoint table and sample `curl` requests.

## Commands

```bash
mvn clean compile          # compile
mvn test                   # run all tests + generate a JaCoCo coverage report
mvn test -Dtest=ClassName                         # run a single test class
mvn test -Dtest=ClassName#methodName              # run a single test method
mvn spring-boot:run        # start the server (port 8081, context path /brite)
```

`mvn test` also runs the `jacoco-maven-plugin` (`prepare-agent` + `report`, version managed by `spring-boot-starter-parent`), producing `target/site/jacoco/index.html` (plus `jacoco.csv`/`jacoco.xml`) — open the HTML file directly in a browser, no server needed. It's build output under `target/`, so it's git-ignored and regenerated on every test run.

**A live MySQL instance is required to run the app or the test suite.** The datasource in `../src/main/resources/application.yml` (`spring.datasource.url: jdbc:mysql://localhost:3306/db_example`) is real — there is no H2/test profile override (`src/test/resources` doesn't exist), so `@SpringBootTest` classes boot the full context against that same MySQL instance. `spring.jpa.hibernate.ddl-auto: update` auto-creates/updates tables (currently just `events`) on startup.

Base URL for every endpoint: `http://localhost:8081/brite` (context path `/brite` is configured in `application.yml`).

## Architecture

### Module layout

Each top-level package under `org.bee` is a self-contained vertical slice (controller → service → repository), independently scanned via `scanBasePackages`/`@ConfigurationPropertiesScan` in `SpringBootProjectsApplication`:

- `org.bee.banking` — client/account registration, withdraw/deposit, bank statements, async notifications, Resilience4j payment demo, a `Filter`-based API gateway/rate limiter (`org.bee.banking.gateway`) fronting the whole module, and paginated/single account-status lookup (`AccountStatusStatementService` in `org.bee.banking.service`, view DTO in `org.bee.banking.domain`)
- `org.bee.retail` — in-memory product catalog
- `org.bee.events` — JSON Schema-validated event ingestion, persisted via JPA
- `org.bee.configs` — `@ConfigurationProperties` classes (`BriteEmailConfigValues`, `BriteSmsNotificationConfigValues`, `BriteApplicationConfigValues`) plus a read-only controller that echoes them back
- `org.bee.restapi.versioning` — API-versioning strategy demos (URI/param/header/content-negotiation)
- `org.bee.sample` — a small standalone lookup demo, unrelated to the other modules

### Mixed persistence: most of the app is in-memory, not MySQL

Despite the MySQL datasource being required at boot, only the **events** module actually persists to it (`EventEntity` via `EventRepository extends JpaRepository`). Banking and retail data live in plain in-memory maps that reset on every restart:

- `AccountRepository` (banking) — a `ConcurrentHashMap` instance field, seeded with demo accounts in the constructor
- `ProductRepository` (retail) — backed by a **`private static Map`** populated once from `LoadProductData.loadproddata()`. Because it's static, mutations from one test (e.g. `addproduct` in a test method) persist for the JVM's lifetime and leak across test methods/classes — be careful writing tests that assert exact catalog size or contents.

### Account number prefix drives account type resolution

Account numbers are the source of truth for account type, not a stored field: `ClientAccountService.withdrawAndSaveToAccount`/`depositAndSaveToAccount` read the first two characters of the account number (`"CH"` → `CHECKING`, `"SV"` → `SAVINGS`) and reject anything else with `UNRECOGNIZED_ACCOUNT_PREFIX`. Any code that generates or accepts account numbers (e.g. `AccountRepository.save()`) must keep producing `CH-`/`SV-`-prefixed IDs, or withdraw/deposit will fail for those accounts even though registration succeeds.

### Account status (ACTIVE/CLOSED) gates withdraw/deposit

`Account` carries `accountStatus` (`AccountStatus.ACTIVE`/`CLOSED`), `createdDate`, and `closedDate`. `AccountRepository.save()` defaults new accounts to `ACTIVE` with `createdDate = today` if not already set. `withdraw()`/`deposit()` throw `AccountClosedException` (mapped to `400` by `BankingExceptionHandler`) if the account is `CLOSED` — this check runs *before* the balance/minimum-balance checks. `POST /v1/api/accounts/{accountNumber}/close` (`ClientAccountController.closeAccount` → `ClientAccountService.closeAccount` → `AccountRepository.closeAccount`) transitions `ACTIVE` → `CLOSED` and stamps `closedDate`; it throws `AccountClosedException` (`400`) if already closed, `AccountNotFoundException` (`404`) if the account doesn't exist. There's no reopen/reactivate path. Seed data in `AccountRepository.seedInitialMockData()` includes two pre-closed accounts (`CH-10004`, `SV-20004`) for demoing the closed state; everything else seeds `ACTIVE`.

### Paginated account search is hand-rolled in-memory, not a Spring Data query

`GET /v1/api/accounts` (`ClientAccountController.listAccounts` → `org.bee.banking.service.AccountStatusStatementService.listAccountStatuses` → `AccountRepository.search`) retrieves accounts filtered by an optional `accountNumber`/`status`/`createdFrom`/`createdTo`/`closedFrom`/`closedTo` and paginated via a standard Spring `Pageable` (`page`/`size`/`sort` request params, auto-resolved since spring-data-commons is on the classpath — no extra config needed). `accountNumber` implements the "single account vs. all accounts" conditional lookup: it's just another AND'd filter (exact match against `checkingAccountNumber`/`savingAccountNumber`, case-insensitive) rather than a separate code path, so a provided-but-out-of-date-range account number still yields an empty page instead of bypassing the other filters — there's exactly one query method and one response shape (`Page<AccountStatusView>`) for both "one account" and "all accounts" callers. Because `AccountRepository` is a plain `ConcurrentHashMap`, not a `JpaRepository`, `search()` does the filtering/sorting/pagination itself in Java streams and wraps the result in a manually-built `PageImpl`, returning `Page<Account>`; there's no database query involved. `dbMockStore.values()` is de-duplicated through a `LinkedHashSet` first, since an `Account` with both a checking and savings number would otherwise be stored (and iterated) under two keys. Only `createdDate`, `closedDate`, `accountStatus`, `checkingAccountNumber`, and `savingAccountNumber` are valid `sort` properties (see `AccountRepository.comparatorFor`) — anything else throws `IllegalArgumentException` (`400`).

The "pull accounts with their status" business logic — date-range validation plus mapping each `Account` to the flattened `org.bee.banking.domain.AccountStatusView` (single `accountNumber`, `accountType`, `accountStatus`, `createdDate`, `closedDate`, `firstName`, `lastName`) — lives in `org.bee.banking.service.AccountStatusStatementService`, deliberately separate from `ClientAccountService`/`AccountRepository`: `AccountRepository.search()`/`findByAccountNumber()` stay pure data-access concerns returning full `Account` entities, and `AccountStatusStatementService` owns the view/projection concern on top of them (`Page<Account>.map(...)` → `Page<AccountStatusView>`, or `Optional<Account>.map(...)` for the single-account lookup). `GET /v1/api/accounts/{accountNumber}/status` is the single-account counterpart to the paginated search, returning one `AccountStatusView` (`404` if not found) — same service, same mapping method (`toView`). If you need another account-status-derived view or report, add it to `AccountStatusStatementService`. This endpoint sits under `/v1/api/accounts`, so it's covered by the banking rate-limit gateway (see below) like every other banking endpoint.

**Naming note:** despite the "statement" name and the fact that these classes started life in an `org.bee.banking.statement` package, `AccountStatusStatementService` and `AccountStatusView` were later moved to `org.bee.banking.service` and `org.bee.banking.domain` respectively (to match this module's usual controller→service→repository/domain layout) — but the test class `AccountStatusStatementServiceTest` still physically lives under `src/test/java/org/bee/banking/statement/` with `package org.bee.banking.statement;`. This mismatch between the test's package/directory and the production classes' packages is intentional-but-inconsistent leftover state; it compiles fine (javac doesn't require package-to-directory matching), so don't "fix" it by moving things back without checking with the user first.

### Centralized messages and scoped exception handling

All banking exception messages *and* Slf4j log templates live in `BankingMessages` (`org.bee.banking.messages`) as `String.format`/`{}`-style constants — grep there before adding a new error/log string in the banking module. `BankingExceptionHandler` is a `@RestControllerAdvice(basePackages = "org.bee.banking")`, so it only intercepts exceptions thrown from banking controllers; `retail` and `events` controllers catch and translate their own errors inline (see `EventController.receiveEvent`'s try/catch mapping `JsonProcessingException` → 400, everything else → 500).

### Banking API gateway / rate limiter runs as a `Filter`, not through controllers

`org.bee.banking.gateway` implements a lightweight API-gateway ingress layer in front of *every* banking controller path (`/v1/api/accounts/*`, `/v1/client/*`, `/v1/payment/*`, `/notify`, `/notify-sms`, `/report` — see the `addUrlPatterns(...)` list in `BankingGatewayConfig`). `BankingRateLimitFilter` (registered via a `FilterRegistrationBean`, **not** `@Component` — annotating it directly would make Spring Boot also auto-register it for `/*`) runs before `DispatcherServlet`, so it writes the HTTP response itself (`response.setStatus(...)` + plain-text body) rather than throwing — `BankingExceptionHandler` never sees a rejected request, since `@RestControllerAdvice` only intercepts exceptions from within controller method execution. Two checks, in order: (1) the request must carry the `banking.rate-limit.customer-header-name` header (default `X-Customer-Id`) → `400` if missing/blank; (2) that customer ID is capped at `banking.rate-limit.requests-per-day` (default 1000) requests/day, tracked in `CustomerRateLimiter`'s in-memory `ConcurrentHashMap<customerId, AtomicInteger>` with a single shared day-rollover check (same pattern as `NotificationService`'s SMS daily limit) → `429` once exhausted. Every response gets `X-RateLimit-Limit`/`X-RateLimit-Remaining` headers. `banking.rate-limit.enabled: false` disables the whole filter. If you add a new banking controller or endpoint, add its path to `BankingGatewayConfig.addUrlPatterns(...)` or it won't be rate-limited.

### Notification gating

`NotificationService.sendEmail`/`sendSms` are `@Async` fire-and-forget methods gated by the bound `BriteEmailConfigValues`/`BriteSmsNotificationConfigValues` (`notification.email.*` / `notification.sms.*` in `application.yml`). SMS additionally enforces `dailyLimit` via an in-memory day-rolling counter in `NotificationService`. Both are invoked from `ClientAccountService` (register, withdraw) and `BankStatementService` (statement generation) — deposit does not currently trigger a notification.

### Resilience4j annotation ordering

`PaymentService.processPayment` stacks `@Retry` above `@CircuitBreaker` deliberately (see the comment on that method): Retry is the outer aspect so each retry attempt still respects the breaker's state, and only `@Retry` declares a `fallbackMethod` — if `@CircuitBreaker` also had one, it would swallow failures before `@Retry` ever saw them.

### Liveness/readiness probes

`management.endpoint.health.probes.enabled: true` (in `application.yml`) turns on Spring Boot's Kubernetes-style health groups, exposed at `/actuator/health/liveness` and `/actuator/health/readiness`. Liveness only reflects `LivenessStateHealthIndicator` (the JVM process), so it stays `UP` even if MySQL is down. Readiness is explicitly configured (`management.endpoint.health.group.readiness.include: readinessState,db`) to also fold in the `DataSourceHealthIndicator`, so it flips to `DOWN` if the MySQL connection is lost — that's deliberate, since this app can't actually serve most endpoints without the datasource. If you add other hard dependencies (e.g. a new external service health indicator), add them to that same `readiness.include` list rather than relying on the default `/actuator/health` aggregate.

### Known package/naming typos (match exactly when searching or importing)

- `org.bee.banking.contoller` (missing an "r") holds all banking controllers, not `controller`
- `org.bee.banking.domain.validtors` (missing an "a") holds the custom `@ValidBirthYear` constraint

## Testing

Most existing tests use `@SpringBootTest` + `MockMvc` (see `ProductControllerTest`, `BriteConfigValuesControllerTest`, `SpringBootProjectsApplicationTests`). Because the full context boots, those tests need MySQL reachable as described above.

The banking module's `AccountRepositoryTest`, `ClientAccountServiceTest`, `AccountStatusStatementServiceTest`, `CustomerRateLimiterTest`, and `BankingRateLimitFilterTest` (`src/test/java/org/bee/banking/...`) are deliberately plain JUnit 5 + Mockito unit tests with **no** `@SpringBootTest` — none of `AccountRepository`, `ClientAccountService`, `AccountStatusStatementService`, `CustomerRateLimiter`, or `BankingRateLimitFilter` have Spring/MySQL dependencies of their own (in-memory maps/counters, plain-POJO collaborators, mocked `HttpServletRequest`/`Response`/`FilterChain`), so these run standalone even without MySQL up. When adding tests for banking business logic (validation rules, status transitions, balance math, rate limiting), prefer this pattern over booting the full context; reserve `@SpringBootTest`/`MockMvc` for controller-layer/wiring tests.
