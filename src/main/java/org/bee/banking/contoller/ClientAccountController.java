package org.bee.banking.contoller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.DepositForm;
import org.bee.banking.request.AccountLookupRequest;
import org.bee.banking.request.AccountRegistrationRequest;
import org.bee.banking.request.WithdrawalRequest;
import org.bee.banking.service.ClientAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/accounts")
@RequiredArgsConstructor
public class ClientAccountController {
    private final ClientAccountService accountService;

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
}
