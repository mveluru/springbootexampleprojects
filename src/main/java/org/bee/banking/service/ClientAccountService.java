package org.bee.banking.service;

import lombok.RequiredArgsConstructor;
import org.bee.banking.component.AccountMapper;
import org.bee.banking.domain.Account;
import org.bee.banking.request.AccountLookupRequest;
import org.bee.banking.repository.AccountRepository;
import org.bee.banking.request.AccountRegistrationRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ClientAccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

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
}
