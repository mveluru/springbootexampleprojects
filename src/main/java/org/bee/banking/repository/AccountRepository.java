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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
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
     * Lists accounts (deduplicated - an account with both a checking and savings
     * number would otherwise appear under each key), optionally filtered by an exact
     * account number, status, and/or a createdDate/closedDate range, sorted and
     * paginated per {@code pageable}. When {@code accountNumber} is given, every other
     * filter still applies - the result is a single-element (or empty) page rather than
     * a special case, so callers get one consistent paginated shape either way.
     * Filtering/sorting/paging all happen in-memory since this is a mock store, not a
     * real query - fine for the seeded/demo data volumes here.
     */
    public Page<Account> search(String accountNumber, AccountStatus status, LocalDate createdFrom, LocalDate createdTo,
                                 LocalDate closedFrom, LocalDate closedTo, Pageable pageable) {
        List<Account> matching = new LinkedHashSet<>(dbMockStore.values()).stream()
                .filter(account -> accountNumber == null
                        || accountNumber.equalsIgnoreCase(account.getCheckingAccountNumber())
                        || accountNumber.equalsIgnoreCase(account.getSavingAccountNumber()))
                .filter(account -> status == null || account.getAccountStatus() == status)
                .filter(account -> createdFrom == null
                        || (account.getCreatedDate() != null && !account.getCreatedDate().isBefore(createdFrom)))
                .filter(account -> createdTo == null
                        || (account.getCreatedDate() != null && !account.getCreatedDate().isAfter(createdTo)))
                .filter(account -> closedFrom == null
                        || (account.getClosedDate() != null && !account.getClosedDate().isBefore(closedFrom)))
                .filter(account -> closedTo == null
                        || (account.getClosedDate() != null && !account.getClosedDate().isAfter(closedTo)))
                .sorted(comparatorFor(pageable.getSort()))
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= matching.size()) {
            return new PageImpl<>(List.of(), pageable, matching.size());
        }
        int end = Math.min(start + pageable.getPageSize(), matching.size());
        return new PageImpl<>(matching.subList(start, end), pageable, matching.size());
    }

    private Comparator<Account> comparatorFor(Sort sort) {
        Comparator<Account> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<Account> propertyComparator = switch (order.getProperty()) {
                case "createdDate" -> Comparator.comparing(Account::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
                case "closedDate" -> Comparator.comparing(Account::getClosedDate, Comparator.nullsLast(Comparator.naturalOrder()));
                case "accountStatus" -> Comparator.comparing(account -> account.getAccountStatus() == null ? "" : account.getAccountStatus().name());
                case "checkingAccountNumber" -> Comparator.comparing(account -> account.getCheckingAccountNumber() == null ? "" : account.getCheckingAccountNumber());
                case "savingAccountNumber" -> Comparator.comparing(account -> account.getSavingAccountNumber() == null ? "" : account.getSavingAccountNumber());
                default -> throw new IllegalArgumentException(String.format(BankingMessages.UNSUPPORTED_SORT_PROPERTY, order.getProperty()));
            };
            if (order.isDescending()) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        // Stable default order (insertion order into the map) when no sort was requested
        return comparator == null ? (a, b) -> 0 : comparator;
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
            // Prefix must be "CH" or "SV" - withdraw/deposit resolve account type from it.
            // Numeric part is zero-padded to at least 10 digits to match the seeded numbering scheme.
            String prefix = account.getAccountType() == AccountType.CHECKING ? "CH" : "SV";
            String generatedNum = prefix + "-" + String.format("%010d", dbMockStore.size() + 10001);
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
     * Seeds dummy profiles ready for immediate lookup. Account numbers are always a
     * "CH-"/"SV-" prefix plus a zero-padded 10-digit number (see {@link #save}, which
     * pads dynamically-generated numbers the same way). createdDate/closedDate are
     * computed relative to LocalDate.now() (not fixed calendar dates), so the seeded
     * "plain" ACTIVE accounts always fall inside AccountStatusStatementService's
     * default 18-month lookback window regardless of when the app is started, while
     * the two seeded CLOSED accounts (CH-0000010004, SV-0000020004) are deliberately
     * older than that window to demonstrate the date-range/status filters needing an
     * explicit range or a wider `months` value.
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
                .checkingAccountNumber("CH-0000088291")
                .checkingBalance(new BigDecimal("2450.75"))
                .accountType(AccountType.CHECKING)
                .accountStatus(AccountStatus.ACTIVE)
                .createdDate(LocalDate.now().minusMonths(2))
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
                .savingAccountNumber("SV-0000044102")
                .savingBalance(new BigDecimal("12800.00"))
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .createdDate(LocalDate.now().minusMonths(4))
                .customer(cust2)
                .build();

        // Save pre-seeded data into mock cache
        dbMockStore.put("CH-0000088291", act1);
        dbMockStore.put("SV-0000044102", act2);

        // Additional seeded checking accounts
        seedChecking("CH-0000010001", new BigDecimal("3200.50"), "Carol", "Davis", LocalDate.of(1978, 3, 15), "500 5th Ave", "Denver", "CO", "80202");
        seedChecking("CH-0000010002", new BigDecimal("1875.20"), "David", "Miller", LocalDate.of(1994, 8, 19), "789 Pine Rd", "Houston", "TX", "77001");
        seedChecking("CH-0000010003", new BigDecimal("4620.00"), "Emma", "Wilson", LocalDate.of(1988, 12, 1), "200 2nd St", "Seattle", "WA", "98101");
        seedChecking("CH-0000010004", new BigDecimal("980.35"), "Frank", "Garcia", LocalDate.of(1975, 6, 23), "100 Ocean Dr", "Miami", "FL", "33101",
                AccountStatus.CLOSED, LocalDate.now().minusMonths(30), LocalDate.now().minusMonths(6));
        seedChecking("CH-0000010005", new BigDecimal("6120.75"), "Grace", "Lee", LocalDate.of(1990, 1, 30), "300 Lake Shore Dr", "Chicago", "IL", "60601");
        seedChecking("CH-0000010006", new BigDecimal("2340.60"), "Henry", "Martinez", LocalDate.of(1982, 9, 5), "150 Desert Rd", "Phoenix", "AZ", "85001");
        seedChecking("CH-0000010007", new BigDecimal("1500.00"), "Ivy", "Chen", LocalDate.of(1996, 4, 11), "45 Beacon St", "Boston", "MA", "02101");
        seedChecking("CH-0000010008", new BigDecimal("7890.10"), "Jack", "Robinson", LocalDate.of(1970, 11, 27), "10 Peachtree St", "Atlanta", "GA", "30301");
        seedChecking("CH-0000010009", new BigDecimal("3450.90"), "Karen", "White", LocalDate.of(1985, 7, 14), "25 Pine St", "Portland", "OR", "97201");
        seedChecking("CH-0000010010", new BigDecimal("2100.45"), "Liam", "Thompson", LocalDate.of(1993, 2, 8), "600 Colfax Ave", "Denver", "CO", "80203");
        seedChecking("CH-0000010011", new BigDecimal("5430.00"), "Xavier", "Brooks", LocalDate.of(1986, 5, 21), "12 Birch St", "Raleigh", "NC", "27601");
        seedChecking("CH-0000010012", new BigDecimal("2890.60"), "Yolanda", "Reyes", LocalDate.of(1991, 9, 14), "77 Cedar Ave", "Tucson", "AZ", "85701");
        seedChecking("CH-0000010013", new BigDecimal("6710.25"), "Zachary", "Foster", LocalDate.of(1979, 2, 3), "300 Elm St", "Kansas City", "MO", "64101");
        seedChecking("CH-0000010014", new BigDecimal("1980.40"), "Amanda", "Price", LocalDate.of(1997, 7, 30), "88 Willow Dr", "Omaha", "NE", "68101");
        seedChecking("CH-0000010015", new BigDecimal("4560.15"), "Brian", "Coleman", LocalDate.of(1984, 11, 8), "45 Aspen Ln", "Boise", "ID", "83701");
        seedChecking("CH-0000010016", new BigDecimal("3320.90"), "Cynthia", "Ortiz", LocalDate.of(1990, 4, 25), "210 Sunset Blvd", "Fresno", "CA", "93701");
        seedChecking("CH-0000010017", new BigDecimal("7120.00"), "Daniel", "Reed", LocalDate.of(1976, 8, 17), "63 Magnolia St", "Tulsa", "OK", "74101");
        seedChecking("CH-0000010018", new BigDecimal("2450.55"), "Elena", "Vargas", LocalDate.of(1993, 12, 9), "18 Riverside Dr", "Albuquerque", "NM", "87101");
        seedChecking("CH-0000010019", new BigDecimal("5980.30"), "Felix", "Ward", LocalDate.of(1981, 3, 27), "500 Highland Ave", "Louisville", "KY", "40201");
        seedChecking("CH-0000010020", new BigDecimal("3140.70"), "Gina", "Torres", LocalDate.of(1988, 6, 13), "27 Meadow Ln", "Baton Rouge", "LA", "70801");
        seedChecking("CH-0000010021", new BigDecimal("6890.45"), "Hassan", "Ali", LocalDate.of(1992, 10, 22), "140 Grove St", "Richmond", "VA", "23218");
        seedChecking("CH-0000010022", new BigDecimal("2670.80"), "Isabel", "Cruz", LocalDate.of(1987, 1, 19), "9 Harbor Way", "Providence", "RI", "02901");
        seedChecking("CH-0000010023", new BigDecimal("4980.20"), "Jerome", "Bell", LocalDate.of(1975, 5, 6), "310 Union St", "Hartford", "CT", "06101");
        seedChecking("CH-0000010024", new BigDecimal("3760.10"), "Kayla", "Simmons", LocalDate.of(1995, 9, 2), "72 Fairview Rd", "Madison", "WI", "53701");
        seedChecking("CH-0000010025", new BigDecimal("5210.65"), "Louis", "Fischer", LocalDate.of(1983, 12, 30), "205 Chestnut St", "Des Moines", "IA", "50301");

        // Additional seeded savings accounts
        seedSavings("SV-0000020001", new BigDecimal("15200.00"), "Maria", "Rodriguez", LocalDate.of(1980, 5, 19), "12 Elm St", "Dallas", "TX", "75201");
        seedSavings("SV-0000020002", new BigDecimal("8900.50"), "Noah", "Anderson", LocalDate.of(1992, 10, 3), "88 Broadway", "San Diego", "CA", "92101");
        seedSavings("SV-0000020003", new BigDecimal("22000.75"), "Olivia", "Harris", LocalDate.of(1987, 3, 22), "5 Music Row", "Nashville", "TN", "37201");
        seedSavings("SV-0000020004", new BigDecimal("5600.30"), "Peter", "Clark", LocalDate.of(1976, 12, 15), "300 High St", "Columbus", "OH", "43201",
                AccountStatus.CLOSED, LocalDate.now().minusMonths(24), LocalDate.now().minusMonths(3));
        seedSavings("SV-0000020005", new BigDecimal("13400.00"), "Quinn", "Lewis", LocalDate.of(1995, 6, 9), "700 Congress Ave", "Austin", "TX", "78702");
        seedSavings("SV-0000020006", new BigDecimal("9800.60"), "Rachel", "Walker", LocalDate.of(1983, 8, 27), "40 Trade St", "Charlotte", "NC", "28201");
        seedSavings("SV-0000020007", new BigDecimal("30500.00"), "Samuel", "Young", LocalDate.of(1971, 1, 12), "120 Fremont St", "Las Vegas", "NV", "89101");
        seedSavings("SV-0000020008", new BigDecimal("4200.15"), "Tina", "Hall", LocalDate.of(1998, 9, 30), "9 Orange Ave", "Orlando", "FL", "32801");
        seedSavings("SV-0000020009", new BigDecimal("17650.40"), "Victor", "King", LocalDate.of(1989, 4, 18), "60 Nicollet Mall", "Minneapolis", "MN", "55401");
        seedSavings("SV-0000020010", new BigDecimal("6700.25"), "Wendy", "Scott", LocalDate.of(1974, 11, 2), "15 Capitol Mall", "Sacramento", "CA", "95814");
        seedSavings("SV-0000020011", new BigDecimal("11200.35"), "Monica", "Diaz", LocalDate.of(1986, 2, 14), "44 Lakeview Dr", "Salt Lake City", "UT", "84101");
        seedSavings("SV-0000020012", new BigDecimal("8650.90"), "Nathan", "Brooks", LocalDate.of(1990, 6, 28), "77 Ridge Rd", "Little Rock", "AR", "72201");
        seedSavings("SV-0000020013", new BigDecimal("19800.00"), "Priya", "Patel", LocalDate.of(1993, 10, 11), "212 Sunrise Ave", "Anchorage", "AK", "99501");
        seedSavings("SV-0000020014", new BigDecimal("7340.55"), "Oscar", "Delgado", LocalDate.of(1979, 4, 7), "63 Pinecrest Ln", "Spokane", "WA", "99201");
        seedSavings("SV-0000020015", new BigDecimal("14500.20"), "Paula", "Nguyen", LocalDate.of(1996, 8, 23), "18 Bayview Ter", "Honolulu", "HI", "96801");
        seedSavings("SV-0000020016", new BigDecimal("9990.10"), "Ryan", "Mitchell", LocalDate.of(1985, 12, 5), "300 Foothill Blvd", "Reno", "NV", "89501");
        seedSavings("SV-0000020017", new BigDecimal("23100.75"), "Sofia", "Ramirez", LocalDate.of(1991, 3, 16), "55 Garden St", "Albany", "NY", "12201");
        seedSavings("SV-0000020018", new BigDecimal("6420.40"), "Trevor", "Hughes", LocalDate.of(1977, 7, 1), "140 Maple Ave", "Burlington", "VT", "05401");
        seedSavings("SV-0000020019", new BigDecimal("16750.00"), "Ursula", "Bennett", LocalDate.of(1994, 11, 29), "9 Overlook Dr", "Jackson", "MS", "39201");
        seedSavings("SV-0000020020", new BigDecimal("10230.85"), "Vincent", "Nolan", LocalDate.of(1982, 5, 20), "270 Canyon Rd", "Cheyenne", "WY", "82001");
        seedSavings("SV-0000020021", new BigDecimal("13890.60"), "Wanda", "Perry", LocalDate.of(1989, 9, 9), "38 Brookside Ave", "Fargo", "ND", "58102");
        seedSavings("SV-0000020022", new BigDecimal("8100.25"), "Xiomara", "Lopez", LocalDate.of(1997, 1, 26), "115 Southgate Dr", "Wichita", "KS", "67201");
        seedSavings("SV-0000020023", new BigDecimal("20450.90"), "Yusuf", "Ibrahim", LocalDate.of(1984, 6, 4), "6 Lighthouse Rd", "Portland", "ME", "04101");
        seedSavings("SV-0000020024", new BigDecimal("7560.35"), "Zoe", "Campbell", LocalDate.of(1992, 10, 18), "82 Timber Ln", "Charleston", "WV", "25301");
        seedSavings("SV-0000020025", new BigDecimal("12980.50"), "Aaron", "Blake", LocalDate.of(1980, 2, 27), "29 Windsor Ct", "Manchester", "NH", "03101");
    }

    private void seedChecking(String accountNumber, BigDecimal balance, String firstName, String lastName,
                               LocalDate dateOfBirth, String street, String city, String state, String zip) {
        seedChecking(accountNumber, balance, firstName, lastName, dateOfBirth, street, city, state, zip,
                AccountStatus.ACTIVE, LocalDate.now().minusMonths(5), null);
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
                AccountStatus.ACTIVE, LocalDate.now().minusMonths(5), null);
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
  "accountNumber": "CH-0000088291"
}
http://localhost:8080/api/accounts/register
{
  "checkingAccountNumber": "CH-0000088291",
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