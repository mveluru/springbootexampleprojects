package org.bee.banking.repository;

import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.Address;
import org.bee.banking.domain.Customer;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
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
        }

        // Index under whichever account number is active
        if (account.getCheckingAccountNumber() != null) {
            dbMockStore.put(account.getCheckingAccountNumber(), account);
        }
        if (account.getSavingAccountNumber() != null) {
            dbMockStore.put(account.getSavingAccountNumber(), account);
        }

        return account;
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