package org.bee.banking.service;

import lombok.RequiredArgsConstructor;
import org.bee.banking.component.AccountMapper;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountType;
import org.bee.banking.repository.WithdrawalRespository;
import org.bee.banking.request.AccountLookupRequest;
import org.bee.banking.repository.AccountRepository;
import org.bee.banking.request.AccountRegistrationRequest;
import org.bee.banking.request.WithdrawalRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientAccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
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

    public void WithdrawAndSaveToAccount(WithdrawalRequest withdrawalRequest) {
        AccountType requestedAcctType = null;
        BigDecimal withDrawAmount = withdrawalRequest.getWithdrawAmount();
        String accountNumber = withdrawalRequest.getAccountNumber();
        if (accountNumber.substring(0, 2).equalsIgnoreCase("CH")) {
            requestedAcctType = AccountType.valueOf("Checking Account");
        } else if (accountNumber.substring(0, 2).equalsIgnoreCase("SV")) {
            requestedAcctType = AccountType.valueOf("Saving Account");
        }
        assert requestedAcctType != null;
        if (requestedAcctType.equals(withdrawalRequest.getAccountType())) {
            Optional<Account> existingAccount = accountRepository.findByAccountNumber(accountNumber);
            if (existingAccount.isPresent() && requestedAcctType.equals(AccountType.CHECKING)) {
                Account existingAccountEntity = existingAccount.get();
                existingAccountEntity.
                        setCheckingBalance(existingAccountEntity.getSavingBalance().subtract(withDrawAmount));
                accountRepository.update(existingAccountEntity);

            } else if (existingAccount.isPresent() && requestedAcctType.equals(AccountType.SAVINGS)) {
                Account existingAccountEntity = existingAccount.get();
                existingAccountEntity.
                        setSavingBalance(existingAccountEntity.getSavingBalance().subtract(withDrawAmount));
                accountRepository.update(existingAccountEntity);
            }
        }
    }
}
