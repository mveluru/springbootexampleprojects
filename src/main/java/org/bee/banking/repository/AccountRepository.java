package org.bee.banking.repository;

import lombok.extern.slf4j.Slf4j;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.Address;
import org.bee.banking.domain.Customer;
import org.bee.banking.exception.AccountNotFoundException;
import org.bee.banking.exception.InsufficientFundsException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class AccountRepository {
    // Simple thread-safe in-memory database simulation
    private final Map<String, Account> dbMockStore = new ConcurrentHashMap<>();

    // Constructor seeds initial mock data for Flow A testing
    public AccountRepository() {
        seedInitialMockData();
    }

    /**
     * Simulation of findByCheckingAccountNumberOrSavingAccountNumber
     */
    public Optional<Account> findByAccountNumber(String accountNumber) {
        if (accountNumber == null) return Optional.empty();
        return Optional.ofNullable(dbMockStore.get(accountNumber));
    }

    /**
     * Simulation of database save()
     */
    public Account save(Account account) {
        // Generate mock account numbers if they don't exist yet
        if (account.getCheckingAccountNumber() == null && account.getSavingAccountNumber() == null) {
            String generatedNum = "ACT-" + (dbMockStore.size() + 10001);
            if (account.getAccountType() == AccountType.CHECKING) {
                account.setCheckingAccountNumber(generatedNum);
                account.setCheckingBalance(BigDecimal.ZERO);
            } else {
                account.setSavingAccountNumber(generatedNum);
                account.setSavingBalance(BigDecimal.ZERO);
            }
            log.info("Generated new {} account number {}", account.getAccountType(), generatedNum);
        }

        // Index under whichever account number is active
        if (account.getCheckingAccountNumber() != null) {
            dbMockStore.put(account.getCheckingAccountNumber(), account);
        }
        if (account.getSavingAccountNumber() != null) {
            dbMockStore.put(account.getSavingAccountNumber(), account);
        }

        log.info("Saved account {}", account.getCheckingAccountNumber() != null
                ? account.getCheckingAccountNumber() : account.getSavingAccountNumber());
        return account;
    }

    public void update(Account account) {
        if (account.getCheckingAccountNumber() != null) {
            dbMockStore.put(account.getCheckingAccountNumber(), account);
        }
        if (account.getSavingAccountNumber() != null) {
            dbMockStore.put(account.getSavingAccountNumber(), account);
        }
        log.debug("Updated account {}", account.getCheckingAccountNumber() != null
                ? account.getCheckingAccountNumber() : account.getSavingAccountNumber());
    }

    /**
     * Atomically debits the checking or savings balance under a single map operation,
     * avoiding the lost-update race of a separate find + mutate + update sequence.
     */
    public Account withdraw(String accountNumber, AccountType accountType, BigDecimal amount) {
        Account updated = dbMockStore.computeIfPresent(accountNumber, (key, account) -> {
            BigDecimal currentBalance = accountType == AccountType.CHECKING
                    ? account.getCheckingBalance()
                    : account.getSavingBalance();
            if (currentBalance == null || currentBalance.compareTo(amount) < 0) {
                log.warn("Withdrawal of {} rejected for account {}: insufficient funds (balance {})",
                        amount, accountNumber, currentBalance);
                throw new InsufficientFundsException("Insufficient funds in account " + accountNumber);
            }
            if (accountType == AccountType.CHECKING) {
                account.setCheckingBalance(currentBalance.subtract(amount));
            } else {
                account.setSavingBalance(currentBalance.subtract(amount));
            }
            return account;
        });
        if (updated == null) {
            log.warn("Withdrawal failed: account {} not found", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        log.info("Withdrew {} from {} account {}", amount, accountType, accountNumber);
        return updated;
    }

    /**
     * Atomically credits the checking or savings balance under a single map operation,
     * avoiding the lost-update race of a separate find + mutate + update sequence.
     */
    public Account deposit(String accountNumber, AccountType accountType, BigDecimal amount) {
        Account updated = dbMockStore.computeIfPresent(accountNumber, (key, account) -> {
            if (accountType == AccountType.CHECKING) {
                BigDecimal currentBalance = account.getCheckingBalance() != null ? account.getCheckingBalance() : BigDecimal.ZERO;
                account.setCheckingBalance(currentBalance.add(amount));
            } else {
                BigDecimal currentBalance = account.getSavingBalance() != null ? account.getSavingBalance() : BigDecimal.ZERO;
                account.setSavingBalance(currentBalance.add(amount));
            }
            return account;
        });
        if (updated == null) {
            log.warn("Deposit failed: account {} not found", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        log.info("Deposited {} into {} account {}", amount, accountType, accountNumber);
        return updated;
    }


    /**
     * Seeds dummy profiles ready for immediate lookup
     */
    private void seedInitialMockData() {
        // Pre-loaded client 1
        Address addr1 = Address.builder()
                .street("123 Main St")
                .city("Austin").state("TX").zip("78701")
                .addressLine1("Apt 4B").country("USA")
                .build();

        Customer cust1 = Customer.builder()
                .firstName("Alice")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1985, 4, 12))
                .address(addr1)
                .build();

        Account act1 = Account.builder()
                .checkingAccountNumber("CH-88291")
                .checkingBalance(new BigDecimal("2450.75"))
                .accountType(AccountType.CHECKING)
                .customer(cust1)
                .build();

        // Pre-loaded client 2
        Address addr2 = Address.builder()
                .street("456 Oak Ln")
                .city("Dallas").state("TX").zip("75201")
                .country("USA")
                .build();

        Customer cust2 = Customer.builder()
                .firstName("Bob")
                .lastName("Jones")
                .dateOfBirth(LocalDate.of(1991, 11, 23))
                .address(addr2)
                .build();

        Account act2 = Account.builder()
                .savingAccountNumber("SV-44102")
                .savingBalance(new BigDecimal("12800.00"))
                .accountType(AccountType.SAVINGS)
                .customer(cust2)
                .build();

        // Save pre-seeded data into mock cache
        dbMockStore.put("CH-88291", act1);
        dbMockStore.put("SV-44102", act2);
    }
}
/*
{
  "firstName": "David",
  "lastName": "Miller",
  "dateOfBirth": "1994-08-19",
  "street": "789 Pine Rd",
  "city": "Houston",
  "state": "TX",
  "zip": "77001",
  "addressLine1": "Suite 12",
  "accountType": "savings"
}
http://localhost:8080/api/accounts/lookup
{
  "accountNumber": "CH-88291"
}
http://localhost:8080/api/accounts/register
{
  "checkingAccountNumber": "CH-88291",
  "savingAccountNumber": null,
  "checkingBalance": 2450.75,
  "savingBalance": null,
  "accountType": "CHECKING",
  "customer": {
    "firstName": "Alice",
    "lastName": "Smith",
    "dateOfBirth": "1985-04-12",
    "address": {
      "street": "123 Main St",
      "city": "Austin",
      "state": "TX",
      "zip": "78701",
      "addressLine1": "Apt 4B",
      "addressLine2": null,
      "country": "USA"
    }
  }
}
 */