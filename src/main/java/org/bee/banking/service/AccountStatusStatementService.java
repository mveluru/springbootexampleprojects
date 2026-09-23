package org.bee.banking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountStatus;
import org.bee.banking.domain.AccountStatusView;
import org.bee.banking.messages.BankingMessages;
import org.bee.banking.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Pulls accounts together with their status/lifecycle dates and owning customer's
 * name, paginated. Owns the account-search validation and the Account -&gt;
 * AccountStatusView mapping; AccountRepository.search() stays purely a data-access
 * concern (filter/sort/paginate the in-memory store), with no knowledge of this view.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountStatusStatementService {
    private final AccountRepository accountRepository;

    public Page<AccountStatusView> listAccountStatuses(AccountStatus status, LocalDate createdFrom, LocalDate createdTo,
                                                       LocalDate closedFrom, LocalDate closedTo, Pageable pageable) {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            String message = String.format(BankingMessages.CREATED_DATE_RANGE_INVALID, createdFrom, createdTo);
            log.warn(BankingMessages.LOG_ACCOUNT_SEARCH_REJECTED_DATE_RANGE, message);
            throw new IllegalArgumentException(message);
        }
        if (closedFrom != null && closedTo != null && closedFrom.isAfter(closedTo)) {
            String message = String.format(BankingMessages.CLOSED_DATE_RANGE_INVALID, closedFrom, closedTo);
            log.warn(BankingMessages.LOG_ACCOUNT_SEARCH_REJECTED_DATE_RANGE, message);
            throw new IllegalArgumentException(message);
        }

        log.debug(BankingMessages.LOG_ACCOUNT_SEARCH, status, createdFrom, createdTo, closedFrom, closedTo, pageable.getPageNumber());
        Page<Account> accounts = accountRepository.search(status, createdFrom, createdTo, closedFrom, closedTo, pageable);
        return accounts.map(this::toView);
    }

    private AccountStatusView toView(Account account) {
        String accountNumber = account.getCheckingAccountNumber() != null
                ? account.getCheckingAccountNumber() : account.getSavingAccountNumber();
        return AccountStatusView.builder()
                .accountNumber(accountNumber)
                .accountType(account.getAccountType())
                .accountStatus(account.getAccountStatus())
                .createdDate(account.getCreatedDate())
                .closedDate(account.getClosedDate())
                .firstName(account.getCustomer() != null ? account.getCustomer().getFirstName() : null)
                .lastName(account.getCustomer() != null ? account.getCustomer().getLastName() : null)
                .build();
    }
}
