package org.bee.banking.contoller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountStatus;
import org.bee.banking.domain.BankStatement;
import org.bee.banking.domain.DepositForm;
import org.bee.banking.request.AccountLookupRequest;
import org.bee.banking.request.AccountRegistrationRequest;
import org.bee.banking.request.WithdrawalRequest;
import org.bee.banking.service.BankStatementService;
import org.bee.banking.service.ClientAccountService;
import org.bee.banking.service.AccountStatusStatementService;
import org.bee.banking.domain.AccountStatusView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/api/accounts")
@RequiredArgsConstructor
public class ClientAccountController {
    private final ClientAccountService accountService;
    private final BankStatementService bankStatementService;
    private final AccountStatusStatementService accountStatusStatementService;

    /**
     * Scenario G: Retrieve account ids/details within a createdDate/closedDate range.
     * If neither {@code createdFrom} nor {@code createdTo} is given, defaults to
     * "as of today minus {@code months} months" (18 months if {@code months} is also
     * omitted); supplying either explicit created-date bound disables that default and
     * {@code months} is ignored. Conditional lookup: if {@code accountNumber} is
     * provided, only that account is returned (still subject to the resolved
     * date-range/status filters); if it's omitted/null, every matching account is
     * returned, paginated.
     * GET /api/accounts?months=6
     * GET /api/accounts?accountNumber=CH-0000088291&status=CLOSED&createdFrom=2021-01-01&createdTo=2021-12-31&page=0&size=20&sort=createdDate,desc
     */
    @GetMapping
    public ResponseEntity<Page<AccountStatusView>> listAccounts(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate closedTo,
            @RequestParam(required = false) Integer months,
            @PageableDefault(size = 20, sort = "createdDate") Pageable pageable) {
        Page<AccountStatusView> accounts = accountStatusStatementService.listAccountStatuses(
                accountNumber, status, createdFrom, createdTo, closedFrom, closedTo, months, pageable);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Scenario A: Lookup customer profile information by Account Number
     * POST /api/accounts/lookup
     */
    @PostMapping("/lookup")
    public ResponseEntity<?> lookupAccount(@Valid @RequestBody AccountLookupRequest request) {
        return accountService.lookupAccountDetails(request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Account number not found in our records."));
    }

    /**
     * Scenario B: Create a brand new account and customer record structure
     * POST /api/accounts/register
     */
    @PostMapping("/register")
    public ResponseEntity<Account> registerAccount(@Valid @RequestBody AccountRegistrationRequest request) {
        Account createdAccount = accountService.registerNewClientAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    /**
     * Scenario C: Withdraw funds from an existing checking or savings account
     * POST /api/accounts/withdraw
     */
    @PostMapping("/withdraw")
    public ResponseEntity<Account> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        Account updatedAccount = accountService.withdrawAndSaveToAccount(request);
        return ResponseEntity.ok(updatedAccount);
    }

    /**
     * Scenario D: Deposit funds into an existing checking or savings account
     * POST /api/accounts/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<Account> deposit(@Valid @RequestBody DepositForm request) {
        Account updatedAccount = accountService.depositAndSaveToAccount(request);
        return ResponseEntity.ok(updatedAccount);
    }

    /**
     * Scenario F: Close an existing checking or savings account
     * POST /api/accounts/{accountNumber}/close
     */
    @PostMapping("/{accountNumber}/close")
    public ResponseEntity<Account> closeAccount(@PathVariable String accountNumber) {
        Account closedAccount = accountService.closeAccount(accountNumber);
        return ResponseEntity.ok(closedAccount);
    }

    /**
     * Scenario E: Generate a bank statement for an account within a date range
     * GET /api/accounts/{accountNumber}/statement?beginDate=yyyy-MM-dd&endDate=yyyy-MM-dd
     */
    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<BankStatement> statement(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beginDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BankStatement statement = bankStatementService.generateStatement(accountNumber, beginDate, endDate);
        return ResponseEntity.ok(statement);
    }
}
