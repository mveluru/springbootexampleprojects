package org.bee.banking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bee.banking.component.AccountMapper;
import org.bee.banking.component.WithdrawalMapper;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.DepositForm;
import org.bee.banking.domain.WithdrawalForm;
import org.bee.banking.messages.StaticMessages;
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
@Slf4j
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
        log.debug(StaticMessages.LOG_ACCOUNT_LOOKUP, request.getAccountNumber());
        Optional<Account> account = accountRepository.findByAccountNumber(request.getAccountNumber());
        if (account.isEmpty()) {
            log.warn(StaticMessages.LOG_ACCOUNT_LOOKUP_FAILED, request.getAccountNumber());
        }
        return account;
    }

    /**
     * Flow B: Register and save brand new profiles dynamically
     */
    public Account registerNewClientAccount(AccountRegistrationRequest request) {
        // MapStruct constructs nested object structure automatically
        Account newAccountEntity = accountMapper.toAccountEntity(request);

        // Commits layout back into our static map structure
        Account savedAccount = accountRepository.save(newAccountEntity);
        log.info(StaticMessages.LOG_ACCOUNT_REGISTERED, savedAccount.getAccountType(),
                savedAccount.getCheckingAccountNumber() != null
                        ? savedAccount.getCheckingAccountNumber() : savedAccount.getSavingAccountNumber());
        return savedAccount;
    }

    @Transactional
    public Account withdrawAndSaveToAccount(WithdrawalRequest withdrawalRequest) {
        String accountNumber = withdrawalRequest.getAccountNumber();
        BigDecimal withdrawAmount = withdrawalRequest.getWithdrawAmount();

        if (accountNumber == null || accountNumber.length() < 2) {
            log.warn(StaticMessages.LOG_WITHDRAWAL_REJECTED_ACCOUNT_NUMBER);
            throw new IllegalArgumentException(StaticMessages.ACCOUNT_NUMBER_REQUIRED);
        }
        if (withdrawAmount == null || withdrawAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(StaticMessages.LOG_WITHDRAWAL_REJECTED_AMOUNT, accountNumber, withdrawAmount);
            throw new IllegalArgumentException(StaticMessages.WITHDRAWAL_AMOUNT_POSITIVE);
        }

        String prefix = accountNumber.substring(0, 2);
        AccountType requestedAcctType;
        if (prefix.equalsIgnoreCase("CH")) {
            requestedAcctType = AccountType.CHECKING;
        } else if (prefix.equalsIgnoreCase("SV")) {
            requestedAcctType = AccountType.SAVINGS;
        } else {
            log.warn(StaticMessages.LOG_WITHDRAWAL_REJECTED_PREFIX, prefix);
            throw new IllegalArgumentException(String.format(StaticMessages.UNRECOGNIZED_ACCOUNT_PREFIX, prefix));
        }

        if (requestedAcctType != withdrawalRequest.getAccountType()) {
            log.warn(StaticMessages.LOG_WITHDRAWAL_REJECTED_TYPE_MISMATCH,
                    accountNumber, withdrawalRequest.getAccountType(), requestedAcctType);
            throw new IllegalArgumentException(StaticMessages.ACCOUNT_TYPE_MISMATCH);
        }

        log.info(StaticMessages.LOG_WITHDRAWAL_PROCESSING, withdrawAmount, requestedAcctType, accountNumber);

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
            log.warn(StaticMessages.LOG_DEPOSIT_REJECTED_ACCOUNT_NUMBER);
            throw new IllegalArgumentException(StaticMessages.ACCOUNT_NUMBER_REQUIRED);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(StaticMessages.LOG_DEPOSIT_REJECTED_AMOUNT, accountNumber, amount);
            throw new IllegalArgumentException(StaticMessages.DEPOSIT_AMOUNT_POSITIVE);
        }

        String prefix = accountNumber.substring(0, 2);
        AccountType requestedAcctType;
        if (prefix.equalsIgnoreCase("CH")) {
            requestedAcctType = AccountType.CHECKING;
        } else if (prefix.equalsIgnoreCase("SV")) {
            requestedAcctType = AccountType.SAVINGS;
        } else {
            log.warn(StaticMessages.LOG_DEPOSIT_REJECTED_PREFIX, prefix);
            throw new IllegalArgumentException(String.format(StaticMessages.UNRECOGNIZED_ACCOUNT_PREFIX, prefix));
        }

        if (requestedAcctType != depositForm.getAccountType()) {
            log.warn(StaticMessages.LOG_DEPOSIT_REJECTED_TYPE_MISMATCH,
                    accountNumber, depositForm.getAccountType(), requestedAcctType);
            throw new IllegalArgumentException(StaticMessages.ACCOUNT_TYPE_MISMATCH);
        }

        String depositType = depositForm.getDepositType();
        if (depositType != null && !depositType.equalsIgnoreCase("cash") && !depositType.equalsIgnoreCase("check")) {
            log.warn(StaticMessages.LOG_DEPOSIT_REJECTED_TYPE_INVALID, accountNumber, depositType);
            throw new IllegalArgumentException(StaticMessages.DEPOSIT_TYPE_INVALID);
        }

        log.info(StaticMessages.LOG_DEPOSIT_PROCESSING, depositType, amount, requestedAcctType, accountNumber);

        // Single atomic repository call avoids the find-then-mutate-then-update race
        // between concurrent deposits on the same account.
        return accountRepository.deposit(accountNumber, requestedAcctType, amount);
    }
}
