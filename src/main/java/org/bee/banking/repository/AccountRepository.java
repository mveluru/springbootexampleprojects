package org.bee.banking.repository;

import lombok.extern.slf4j.Slf4j;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountStatus;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.Address;
import org.bee.banking.domain.Customer;
import org.bee.banking.exception.AccountClosedException;
import org.bee.banking.exception.AccountNotFoundException;
import org.bee.banking.exception.InsufficientFundsException;
import org.bee.banking.exception.MinBalanceException;
import org.bee.banking.messages.BankingMessages;
import org.bee.banking.rules.AccountConstraints;
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
    private final AccountConstraints accountConstraints;

    // Constructor seeds initial mock data for Flow A testing
    public AccountRepository(AccountConstraints accountConstraints) {
        this.accountConstraints = accountConstraints;
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
        // Newly created accounts start out ACTIVE with today as their creation date
        if (account.getAccountStatus() == null) {
            account.setAccountStatus(AccountStatus.ACTIVE);
        }
        if (account.getCreatedDate() == null) {
            account.setCreatedDate(LocalDate.now());
        }

        // Generate mock account numbers if they don't exist yet
        if (account.getCheckingAccountNumber() == null && account.getSavingAccountNumber() == null) {
            // Prefix must be "CH" or "SV" - withdraw/deposit resolve account type from it
            String prefix = account.getAccountType() == AccountType.CHECKING ? "CH" : "SV";
            String generatedNum = prefix + "-" + (dbMockStore.size() + 10001);
            if (account.getAccountType() == AccountType.CHECKING) {
                account.setCheckingAccountNumber(generatedNum);
                account.setCheckingBalance(BigDecimal.ZERO);
            } else {
                account.setSavingAccountNumber(generatedNum);
                account.setSavingBalance(BigDecimal.ZERO);
            }
            log.info(BankingMessages.LOG_ACCOUNT_NUMBER_GENERATED, account.getAccountType(), generatedNum);
        }

        // Index under whichever account number is active
        if (account.getCheckingAccountNumber() != null) {
            dbMockStore.put(account.getCheckingAccountNumber(), account);
        }
        if (account.getSavingAccountNumber() != null) {
            dbMockStore.put(account.getSavingAccountNumber(), account);
        }

        log.info(BankingMessages.LOG_ACCOUNT_SAVED, account.getCheckingAccountNumber() != null
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
        log.debug(BankingMessages.LOG_ACCOUNT_UPDATED, account.getCheckingAccountNumber() != null
                ? account.getCheckingAccountNumber() : account.getSavingAccountNumber());
    }

    /**
     * Atomically debits the checking or savings balance under a single map operation,
     * avoiding the lost-update race of a separate find + mutate + update sequence.
     */
    public Account withdraw(String accountNumber, AccountType accountType, BigDecimal amount) {
        Account updated = dbMockStore.computeIfPresent(accountNumber, (key, account) -> {
            if (account.getAccountStatus() == AccountStatus.CLOSED) {
                log.warn(BankingMessages.LOG_WITHDRAWAL_REJECTED_CLOSED, accountNumber);
                throw new AccountClosedException(String.format(BankingMessages.ACCOUNT_CLOSED, accountNumber));
            }
            BigDecimal currentBalance = accountType == AccountType.CHECKING
                    ? account.getCheckingBalance()
                    : account.getSavingBalance();
            if (currentBalance == null || currentBalance.compareTo(amount) < 0) {
                log.warn(BankingMessages.LOG_WITHDRAWAL_INSUFFICIENT_FUNDS, amount, accountNumber, currentBalance);
                throw new InsufficientFundsException(String.format(BankingMessages.INSUFFICIENT_FUNDS, accountNumber));
            }
            BigDecimal newBalance = currentBalance.subtract(amount);
            BigDecimal minimumBalance = accountType == AccountType.CHECKING
                    ? accountConstraints.getCheckingMinimumBalance()
                    : accountConstraints.getSavingMinimumBalance();
            if (minimumBalance != null && newBalance.compareTo(minimumBalance) < 0) {
                log.warn(BankingMessages.LOG_WITHDRAWAL_BELOW_MINIMUM_BALANCE, amount, accountNumber, newBalance, minimumBalance);
                throw new MinBalanceException(String.format(BankingMessages.MIN_BALANCE_VIOLATION, accountNumber, minimumBalance));
            }
            if (accountType == AccountType.CHECKING) {
                account.setCheckingBalance(newBalance);
            } else {
                account.setSavingBalance(newBalance);
            }
            return account;
        });
        if (updated == null) {
            log.warn(BankingMessages.LOG_WITHDRAWAL_ACCOUNT_NOT_FOUND, accountNumber);
            throw new AccountNotFoundException(String.format(BankingMessages.ACCOUNT_NOT_FOUND, accountNumber));
        }
        log.info(BankingMessages.LOG_WITHDRAWAL_SUCCESS, amount, accountType, accountNumber);
        return updated;
    }

    /**
     * Atomically credits the checking or savings balance under a single map operation,
     * avoiding the lost-update race of a separate find + mutate + update sequence.
     */
    public Account deposit(String accountNumber, AccountType accountType, BigDecimal amount) {
        Account updated = dbMockStore.computeIfPresent(accountNumber, (key, account) -> {
            if (account.getAccountStatus() == AccountStatus.CLOSED) {
                log.warn(BankingMessages.LOG_DEPOSIT_REJECTED_CLOSED, accountNumber);
                throw new AccountClosedException(String.format(BankingMessages.ACCOUNT_CLOSED, accountNumber));
            }
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
            log.warn(BankingMessages.LOG_DEPOSIT_ACCOUNT_NOT_FOUND, accountNumber);
            throw new AccountNotFoundException(String.format(BankingMessages.ACCOUNT_NOT_FOUND, accountNumber));
        }
        log.info(BankingMessages.LOG_DEPOSIT_SUCCESS, amount, accountType, accountNumber);
        return updated;
    }


    /**
     * Marks an account CLOSED and stamps the closure date. Idempotent operations
     * (withdraw/deposit) already check status, so closing just flips the flag here.
     */
    public Account closeAccount(String accountNumber) {
        Account updated = dbMockStore.computeIfPresent(accountNumber, (key, account) -> {
            if (account.getAccountStatus() == AccountStatus.CLOSED) {
                log.warn(BankingMessages.LOG_ACCOUNT_CLOSE_REJECTED_ALREADY_CLOSED, accountNumber);
                throw new AccountClosedException(String.format(BankingMessages.ACCOUNT_ALREADY_CLOSED, accountNumber));
            }
            account.setAccountStatus(AccountStatus.CLOSED);
            account.setClosedDate(LocalDate.now());
            return account;
        });
        if (updated == null) {
            log.warn(BankingMessages.LOG_ACCOUNT_CLOSE_ACCOUNT_NOT_FOUND, accountNumber);
            throw new AccountNotFoundException(String.format(BankingMessages.ACCOUNT_NOT_FOUND, accountNumber));
        }
        log.info(BankingMessages.LOG_ACCOUNT_CLOSED, accountNumber);
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
                .accountStatus(AccountStatus.ACTIVE)
                .createdDate(LocalDate.of(2020, 3, 10))
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
                .accountStatus(AccountStatus.ACTIVE)
                .createdDate(LocalDate.of(2019, 7, 22))
                .customer(cust2)
                .build();

        // Save pre-seeded data into mock cache
        dbMockStore.put("CH-88291", act1);
        dbMockStore.put("SV-44102", act2);

        // Additional seeded checking accounts
        seedChecking("CH-10001", new BigDecimal("3200.50"), "Carol", "Davis", LocalDate.of(1978, 3, 15), "500 5th Ave", "Denver", "CO", "80202");
        seedChecking("CH-10002", new BigDecimal("1875.20"), "David", "Miller", LocalDate.of(1994, 8, 19), "789 Pine Rd", "Houston", "TX", "77001");
        seedChecking("CH-10003", new BigDecimal("4620.00"), "Emma", "Wilson", LocalDate.of(1988, 12, 1), "200 2nd St", "Seattle", "WA", "98101");
        seedChecking("CH-10004", new BigDecimal("980.35"), "Frank", "Garcia", LocalDate.of(1975, 6, 23), "100 Ocean Dr", "Miami", "FL", "33101",
                AccountStatus.CLOSED, LocalDate.of(2022, 4, 1), LocalDate.of(2025, 1, 15));
        seedChecking("CH-10005", new BigDecimal("6120.75"), "Grace", "Lee", LocalDate.of(1990, 1, 30), "300 Lake Shore Dr", "Chicago", "IL", "60601");
        seedChecking("CH-10006", new BigDecimal("2340.60"), "Henry", "Martinez", LocalDate.of(1982, 9, 5), "150 Desert Rd", "Phoenix", "AZ", "85001");
        seedChecking("CH-10007", new BigDecimal("1500.00"), "Ivy", "Chen", LocalDate.of(1996, 4, 11), "45 Beacon St", "Boston", "MA", "02101");
        seedChecking("CH-10008", new BigDecimal("7890.10"), "Jack", "Robinson", LocalDate.of(1970, 11, 27), "10 Peachtree St", "Atlanta", "GA", "30301");
        seedChecking("CH-10009", new BigDecimal("3450.90"), "Karen", "White", LocalDate.of(1985, 7, 14), "25 Pine St", "Portland", "OR", "97201");
        seedChecking("CH-10010", new BigDecimal("2100.45"), "Liam", "Thompson", LocalDate.of(1993, 2, 8), "600 Colfax Ave", "Denver", "CO", "80203");

        // Additional seeded savings accounts
        seedSavings("SV-20001", new BigDecimal("15200.00"), "Maria", "Rodriguez", LocalDate.of(1980, 5, 19), "12 Elm St", "Dallas", "TX", "75201");
        seedSavings("SV-20002", new BigDecimal("8900.50"), "Noah", "Anderson", LocalDate.of(1992, 10, 3), "88 Broadway", "San Diego", "CA", "92101");
        seedSavings("SV-20003", new BigDecimal("22000.75"), "Olivia", "Harris", LocalDate.of(1987, 3, 22), "5 Music Row", "Nashville", "TN", "37201");
        seedSavings("SV-20004", new BigDecimal("5600.30"), "Peter", "Clark", LocalDate.of(1976, 12, 15), "300 High St", "Columbus", "OH", "43201",
                AccountStatus.CLOSED, LocalDate.of(2021, 9, 12), LocalDate.of(2024, 11, 30));
        seedSavings("SV-20005", new BigDecimal("13400.00"), "Quinn", "Lewis", LocalDate.of(1995, 6, 9), "700 Congress Ave", "Austin", "TX", "78702");
        seedSavings("SV-20006", new BigDecimal("9800.60"), "Rachel", "Walker", LocalDate.of(1983, 8, 27), "40 Trade St", "Charlotte", "NC", "28201");
        seedSavings("SV-20007", new BigDecimal("30500.00"), "Samuel", "Young", LocalDate.of(1971, 1, 12), "120 Fremont St", "Las Vegas", "NV", "89101");
        seedSavings("SV-20008", new BigDecimal("4200.15"), "Tina", "Hall", LocalDate.of(1998, 9, 30), "9 Orange Ave", "Orlando", "FL", "32801");
        seedSavings("SV-20009", new BigDecimal("17650.40"), "Victor", "King", LocalDate.of(1989, 4, 18), "60 Nicollet Mall", "Minneapolis", "MN", "55401");
        seedSavings("SV-20010", new BigDecimal("6700.25"), "Wendy", "Scott", LocalDate.of(1974, 11, 2), "15 Capitol Mall", "Sacramento", "CA", "95814");
    }

    private void seedChecking(String accountNumber, BigDecimal balance, String firstName, String lastName,
                               LocalDate dateOfBirth, String street, String city, String state, String zip) {
        seedChecking(accountNumber, balance, firstName, lastName, dateOfBirth, street, city, state, zip,
                AccountStatus.ACTIVE, LocalDate.of(2021, 6, 1), null);
    }

    private void seedChecking(String accountNumber, BigDecimal balance, String firstName, String lastName,
                               LocalDate dateOfBirth, String street, String city, String state, String zip,
                               AccountStatus accountStatus, LocalDate createdDate, LocalDate closedDate) {
        Address address = Address.builder().street(street).city(city).state(state).zip(zip).addressLine1(street).country("USA").build();
        Customer customer = Customer.builder().firstName(firstName).lastName(lastName).dateOfBirth(dateOfBirth).address(address).build();
        Account account = Account.builder()
                .checkingAccountNumber(accountNumber)
                .checkingBalance(balance)
                .accountType(AccountType.CHECKING)
                .accountStatus(accountStatus)
                .createdDate(createdDate)
                .closedDate(closedDate)
                .customer(customer)
                .build();
        dbMockStore.put(accountNumber, account);
    }

    private void seedSavings(String accountNumber, BigDecimal balance, String firstName, String lastName,
                              LocalDate dateOfBirth, String street, String city, String state, String zip) {
        seedSavings(accountNumber, balance, firstName, lastName, dateOfBirth, street, city, state, zip,
                AccountStatus.ACTIVE, LocalDate.of(2021, 6, 1), null);
    }

    private void seedSavings(String accountNumber, BigDecimal balance, String firstName, String lastName,
                              LocalDate dateOfBirth, String street, String city, String state, String zip,
                              AccountStatus accountStatus, LocalDate createdDate, LocalDate closedDate) {
        Address address = Address.builder().street(street).city(city).state(state).zip(zip).addressLine1(street).country("USA").build();
        Customer customer = Customer.builder().firstName(firstName).lastName(lastName).dateOfBirth(dateOfBirth).address(address).build();
        Account account = Account.builder()
                .savingAccountNumber(accountNumber)
                .savingBalance(balance)
                .accountType(AccountType.SAVINGS)
                .accountStatus(accountStatus)
                .createdDate(createdDate)
                .closedDate(closedDate)
                .customer(customer)
                .build();
        dbMockStore.put(accountNumber, account);
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