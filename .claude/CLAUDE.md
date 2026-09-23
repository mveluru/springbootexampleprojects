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

- `org.bee.banking` — client/account registration, withdraw/deposit, bank statements, async notifications, Resilience4j payment demo
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

### Centralized messages and scoped exception handling

All banking exception messages *and* Slf4j log templates live in `BankingMessages` (`org.bee.banking.messages`) as `String.format`/`{}`-style constants — grep there before adding a new error/log string in the banking module. `BankingExceptionHandler` is a `@RestControllerAdvice(basePackages = "org.bee.banking")`, so it only intercepts exceptions thrown from banking controllers; `retail` and `events` controllers catch and translate their own errors inline (see `EventController.receiveEvent`'s try/catch mapping `JsonProcessingException` → 400, everything else → 500).

### Notification gating

`NotificationService.sendEmail`/`sendSms` are `@Async` fire-and-forget methods gated by the bound `BriteEmailConfigValues`/`BriteSmsNotificationConfigValues` (`notification.email.*` / `notification.sms.*` in `application.yml`). SMS additionally enforces `dailyLimit` via an in-memory day-rolling counter in `NotificationService`. Both are invoked from `ClientAccountService` (register, withdraw) and `BankStatementService` (statement generation) — deposit does not currently trigger a notification.

### Resilience4j annotation ordering

`PaymentService.processPayment` stacks `@Retry` above `@CircuitBreaker` deliberately (see the comment on that method): Retry is the outer aspect so each retry attempt still respects the breaker's state, and only `@Retry` declares a `fallbackMethod` — if `@CircuitBreaker` also had one, it would swallow failures before `@Retry` ever saw them.

### Known package/naming typos (match exactly when searching or importing)

- `org.bee.banking.contoller` (missing an "r") holds all banking controllers, not `controller`
- `org.bee.banking.domain.validtors` (missing an "a") holds the custom `@ValidBirthYear` constraint

## Testing

Most existing tests use `@SpringBootTest` + `MockMvc` (see `ProductControllerTest`, `BriteConfigValuesControllerTest`, `SpringBootProjectsApplicationTests`). Because the full context boots, those tests need MySQL reachable as described above.

The banking module's `AccountRepositoryTest` and `ClientAccountServiceTest` (`src/test/java/org/bee/banking/...`) are deliberately plain JUnit 5 + Mockito unit tests with **no** `@SpringBootTest` — `AccountRepository` and `ClientAccountService` have no Spring/MySQL dependencies of their own (in-memory map, plain-POJO collaborators), so these run standalone even without MySQL up. When adding tests for banking business logic (validation rules, status transitions, balance math), prefer this pattern over booting the full context; reserve `@SpringBootTest`/`MockMvc` for controller-layer/wiring tests.
