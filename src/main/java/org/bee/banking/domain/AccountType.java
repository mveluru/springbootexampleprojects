package org.bee.banking.domain;

public enum AccountType {
    CHECKING("Checking Account"),
    SAVINGS("Savings Account"),
    INVESTMENT("Brokerage Investment"),
    RETIREMENT("Retirement Portfolio"),
    CREDIT_OR_LOAN("Line of Credit / Loan");

    private final String displayName;

    // Enum Constructor
    AccountType(String displayName) {
        this.displayName = displayName;
    }

    // Getter
    public String getDisplayName() {
        return displayName;
    }
}
