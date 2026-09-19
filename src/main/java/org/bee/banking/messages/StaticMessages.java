package org.bee.banking.messages;

public final class StaticMessages {

    private StaticMessages() {
    }

    public static final String ACCOUNT_NUMBER_REQUIRED = "Account number must be provided and start with CH or SV";
    public static final String WITHDRAWAL_AMOUNT_POSITIVE = "Withdrawal amount must be positive";
    public static final String DEPOSIT_AMOUNT_POSITIVE = "Deposit amount must be positive";
    public static final String UNRECOGNIZED_ACCOUNT_PREFIX = "Unrecognized account number prefix: %s";
    public static final String ACCOUNT_TYPE_MISMATCH = "Requested account type does not match account number";
    public static final String DEPOSIT_TYPE_INVALID = "Deposit type must be 'cash' or 'check'";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds in account %s";
    public static final String ACCOUNT_NOT_FOUND = "Account not found: %s";
}
