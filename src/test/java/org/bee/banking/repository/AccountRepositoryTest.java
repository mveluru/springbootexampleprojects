package org.bee.banking.repository;

import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountStatus;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.Address;
import org.bee.banking.domain.Customer;
import org.bee.banking.exception.AccountClosedException;
import org.bee.banking.exception.AccountNotFoundException;
import org.bee.banking.exception.InsufficientFundsException;
import org.bee.banking.exception.MinBalanceException;
import org.bee.banking.rules.AccountConstraints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Plain unit tests (no Spring context) for the in-memory AccountRepository.
 * AccountRepository has no Spring/MySQL dependencies of its own, so these run
 * without the live MySQL instance the rest of the suite requires.
 */
class AccountRepositoryTest {

    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        AccountConstraints constraints = AccountConstraints.builder()
                .checkingMinimumBalance(BigDecimal.ZERO)
                .savingMinimumBalance(BigDecimal.ZERO)
                .maximumDepositAmountByCash(BigDecimal.valueOf(10_000))
                .minimumAge(18)
                .maxStatementRangeMonths(12)
                .build();
        // Constructor seeds demo data (CH-88291, SV-44102, ...); irrelevant to these tests
        // since each test uses its own freshly-created account number.
        accountRepository = new AccountRepository(constraints);
    }

    private Account newCheckingAccount() {
        Customer customer = Customer.builder()
                .firstName("Test")
                .lastName("User")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address(Address.builder().street("1 Main St").city("Austin").state("TX").zip("78701").country("USA").build())
                .build();
        return Account.builder()
                .accountType(AccountType.CHECKING)
                .customer(customer)
                .build();
    }

    @Test
    void save_newAccount_defaultsToActiveStatusWithTodayAsCreatedDate() {
        Account saved = accountRepository.save(newCheckingAccount());

        assertThat(saved.getCheckingAccountNumber()).startsWith("CH-");
        assertThat(saved.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getCreatedDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getCheckingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void withdraw_activeAccount_reducesBalance() {
        Account saved = accountRepository.save(newCheckingAccount());
        String accountNumber = saved.getCheckingAccountNumber();
        accountRepository.deposit(accountNumber, AccountType.CHECKING, new BigDecimal("500.00"));

        Account updated = accountRepository.withdraw(accountNumber, AccountType.CHECKING, new BigDecimal("200.00"));

        assertThat(updated.getCheckingBalance()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void withdraw_closedAccount_throwsAccountClosedException() {
        Account saved = accountRepository.save(newCheckingAccount());
        String accountNumber = saved.getCheckingAccountNumber();
        accountRepository.closeAccount(accountNumber);

        assertThatThrownBy(() -> accountRepository.withdraw(accountNumber, AccountType.CHECKING, BigDecimal.TEN))
                .isInstanceOf(AccountClosedException.class)
                .hasMessageContaining(accountNumber);
    }

    @Test
    void withdraw_amountExceedsBalance_throwsInsufficientFundsException() {
        Account saved = accountRepository.save(newCheckingAccount());
        String accountNumber = saved.getCheckingAccountNumber();

        assertThatThrownBy(() -> accountRepository.withdraw(accountNumber, AccountType.CHECKING, new BigDecimal("50.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void withdraw_resultingBalanceBelowMinimum_throwsMinBalanceException() {
        AccountConstraints strictConstraints = AccountConstraints.builder()
                .checkingMinimumBalance(new BigDecimal("100.00"))
                .savingMinimumBalance(BigDecimal.ZERO)
                .maximumDepositAmountByCash(BigDecimal.valueOf(10_000))
                .minimumAge(18)
                .build();
        AccountRepository repository = new AccountRepository(strictConstraints);
        Account saved = repository.save(newCheckingAccount());
        String accountNumber = saved.getCheckingAccountNumber();
        repository.deposit(accountNumber, AccountType.CHECKING, new BigDecimal("150.00"));

        assertThatThrownBy(() -> repository.withdraw(accountNumber, AccountType.CHECKING, new BigDecimal("60.00")))
                .isInstanceOf(MinBalanceException.class);
    }

    @Test
    void withdraw_unknownAccount_throwsAccountNotFoundException() {
        assertThatThrownBy(() -> accountRepository.withdraw("CH-00000", AccountType.CHECKING, BigDecimal.TEN))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void deposit_activeAccount_increasesBalance() {
        Account saved = accountRepository.save(newCheckingAccount());
        String accountNumber = saved.getCheckingAccountNumber();

        Account updated = accountRepository.deposit(accountNumber, AccountType.CHECKING, new BigDecimal("75.00"));

        assertThat(updated.getCheckingBalance()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    void deposit_closedAccount_throwsAccountClosedException() {
        Account saved = accountRepository.save(newCheckingAccount());
        String accountNumber = saved.getCheckingAccountNumber();
        accountRepository.closeAccount(accountNumber);

        assertThatThrownBy(() -> accountRepository.deposit(accountNumber, AccountType.CHECKING, BigDecimal.TEN))
                .isInstanceOf(AccountClosedException.class)
                .hasMessageContaining(accountNumber);
    }

    @Test
    void deposit_unknownAccount_throwsAccountNotFoundException() {
        assertThatThrownBy(() -> accountRepository.deposit("CH-00000", AccountType.CHECKING, BigDecimal.TEN))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void closeAccount_activeAccount_transitionsToClosedAndStampsClosedDate() {
        Account saved = accountRepository.save(newCheckingAccount());
        String accountNumber = saved.getCheckingAccountNumber();

        Account closed = accountRepository.closeAccount(accountNumber);

        assertThat(closed.getAccountStatus()).isEqualTo(AccountStatus.CLOSED);
        assertThat(closed.getClosedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void closeAccount_alreadyClosed_throwsAccountClosedException() {
        Account saved = accountRepository.save(newCheckingAccount());
        String accountNumber = saved.getCheckingAccountNumber();
        accountRepository.closeAccount(accountNumber);

        assertThatThrownBy(() -> accountRepository.closeAccount(accountNumber))
                .isInstanceOf(AccountClosedException.class)
                .hasMessageContaining(accountNumber);
    }

    @Test
    void closeAccount_unknownAccount_throwsAccountNotFoundException() {
        assertThatThrownBy(() -> accountRepository.closeAccount("CH-00000"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void findByAccountNumber_seededMockAccount_isFound() {
        assertThat(accountRepository.findByAccountNumber("CH-88291")).isPresent();
    }

    @Test
    void findByAccountNumber_unknownAccount_isEmpty() {
        assertThat(accountRepository.findByAccountNumber("CH-does-not-exist")).isEmpty();
    }
}
