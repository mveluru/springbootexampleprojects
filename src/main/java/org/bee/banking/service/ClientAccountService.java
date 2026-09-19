package org.bee.banking.service;

import lombok.RequiredArgsConstructor;
import org.bee.banking.component.AccountMapper;
import org.bee.banking.component.WithdrawalMapper;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.DepositForm;
import org.bee.banking.domain.WithdrawalForm;
import org.bee.banking.repository.WithdrawalRespository;
import org.bee.banking.request.AccountLookupRequest;
import org.bee.banking.repository.AccountRepository;
import org.bee.banking.request.AccountRegistrationRequest;
import org.bee.banking.request.WithdrawalRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientAccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final WithdrawalMapper withdrawalMapper;
    private final WithdrawalRespository withdrawalRespository;

    /**
     * Flow A: Look up consumer account details
     */
    public Optional<Account> lookupAccountDetails(AccountLookupRequest request) {
        return accountRepository.findByAccountNumber(request.getAccountNumber());
    }

    /**
     * Flow B: Register and save brand new profiles dynamically
     */
    public Account registerNewClientAccount(AccountRegistrationRequest request) {
        // MapStruct constructs nested object structure automatically
        Account newAccountEntity = accountMapper.toAccountEntity(request);

        // Commits layout back into our static map structure
        return accountRepository.save(newAccountEntity);
    }

    @Transactional
    public Account withdrawAndSaveToAccount(WithdrawalRequest withdrawalRequest) {
        String accountNumber = withdrawalRequest.getAccountNumber();
        BigDecimal withdrawAmount = withdrawalRequest.getWithdrawAmount();

        if (accountNumber == null || accountNumber.length() < 2) {
            throw new IllegalArgumentException("Account number must be provided and start with CH or SV");
        }
        if (withdrawAmount == null || withdrawAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        String prefix = accountNumber.substring(0, 2);
        AccountType requestedAcctType;
        if (prefix.equalsIgnoreCase("CH")) {
            requestedAcctType = AccountType.CHECKING;
        } else if (prefix.equalsIgnoreCase("SV")) {
            requestedAcctType = AccountType.SAVINGS;
        } else {
            throw new IllegalArgumentException("Unrecognized account number prefix: " + prefix);
        }

        if (requestedAcctType != withdrawalRequest.getAccountType()) {
            throw new IllegalArgumentException("Requested account type does not match account number");
        }

        // Single atomic repository call avoids the find-then-mutate-then-update race
        // between concurrent withdrawals on the same account.
        Account updatedAccount = accountRepository.withdraw(accountNumber, requestedAcctType, withdrawAmount);

        WithdrawalForm historyRecord = new WithdrawalForm(
                accountNumber,
                requestedAcctType,
                LocalDate.now(),
                withdrawAmount,
                "COMPLETED",
                withdrawalRequest.getFirstName(),
                withdrawalRequest.getLastName(),
                withdrawalMapper.toWithdrawalCustomerAddressEntity(withdrawalRequest)
        );
        withdrawalRespository.createWithdrawal(historyRecord);

        return updatedAccount;
    }

    @Transactional
    public Account depositAndSaveToAccount(DepositForm depositForm) {
        String accountNumber = depositForm.getAccountNumber();
        BigDecimal amount = depositForm.getAmount();

        if (accountNumber == null || accountNumber.length() < 2) {
            throw new IllegalArgumentException("Account number must be provided and start with CH or SV");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        String prefix = accountNumber.substring(0, 2);
        AccountType requestedAcctType;
        if (prefix.equalsIgnoreCase("CH")) {
            requestedAcctType = AccountType.CHECKING;
        } else if (prefix.equalsIgnoreCase("SV")) {
            requestedAcctType = AccountType.SAVINGS;
        } else {
            throw new IllegalArgumentException("Unrecognized account number prefix: " + prefix);
        }

        if (requestedAcctType != depositForm.getAccountType()) {
            throw new IllegalArgumentException("Requested account type does not match account number");
        }

        String depositType = depositForm.getDepositType();
        if (depositType != null && !depositType.equalsIgnoreCase("cash") && !depositType.equalsIgnoreCase("check")) {
            throw new IllegalArgumentException("Deposit type must be 'cash' or 'check'");
        }

        // Single atomic repository call avoids the find-then-mutate-then-update race
        // between concurrent deposits on the same account.
        return accountRepository.deposit(accountNumber, requestedAcctType, amount);
    }
}
