package org.bee.banking.messages;

public final class StaticMessages {

    private StaticMessages() {
    }

    // Exception messages (String.format-style, %s placeholders)
    public static final String ACCOUNT_NUMBER_REQUIRED = "Account number must be provided and start with CH or SV";
    public static final String WITHDRAWAL_AMOUNT_POSITIVE = "Withdrawal amount must be positive";
    public static final String DEPOSIT_AMOUNT_POSITIVE = "Deposit amount must be positive";
    public static final String UNRECOGNIZED_ACCOUNT_PREFIX = "Unrecognized account number prefix: %s";
    public static final String ACCOUNT_TYPE_MISMATCH = "Requested account type does not match account number";
    public static final String DEPOSIT_TYPE_INVALID = "Deposit type must be 'cash' or 'check'";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds in account %s";
    public static final String ACCOUNT_NOT_FOUND = "Account not found: %s";

    // AccountRepository log messages (Slf4j-style, {} placeholders)
    public static final String LOG_ACCOUNT_NUMBER_GENERATED = "Generated new {} account number {}";
    public static final String LOG_ACCOUNT_SAVED = "Saved account {}";
    public static final String LOG_ACCOUNT_UPDATED = "Updated account {}";
    public static final String LOG_WITHDRAWAL_INSUFFICIENT_FUNDS = "Withdrawal of {} rejected for account {}: insufficient funds (balance {})";
    public static final String LOG_WITHDRAWAL_ACCOUNT_NOT_FOUND = "Withdrawal failed: account {} not found";
    public static final String LOG_WITHDRAWAL_SUCCESS = "Withdrew {} from {} account {}";
    public static final String LOG_DEPOSIT_ACCOUNT_NOT_FOUND = "Deposit failed: account {} not found";
    public static final String LOG_DEPOSIT_SUCCESS = "Deposited {} into {} account {}";

    // WithdrawalRespository log messages
    public static final String LOG_WITHDRAWAL_HISTORY_RECORDED = "Recorded withdrawal history entry for account {}: amount={}, status={}";

    // ClientAccountService log messages
    public static final String LOG_ACCOUNT_LOOKUP = "Looking up account {}";
    public static final String LOG_ACCOUNT_LOOKUP_FAILED = "Account lookup failed: {} not found";
    public static final String LOG_ACCOUNT_REGISTERED = "Registered new {} account {}";
    public static final String LOG_WITHDRAWAL_REJECTED_ACCOUNT_NUMBER = "Withdrawal rejected: missing/malformed account number";
    public static final String LOG_WITHDRAWAL_REJECTED_AMOUNT = "Withdrawal rejected for account {}: amount must be positive, got {}";
    public static final String LOG_WITHDRAWAL_REJECTED_PREFIX = "Withdrawal rejected: unrecognized account number prefix {}";
    public static final String LOG_WITHDRAWAL_REJECTED_TYPE_MISMATCH = "Withdrawal rejected for account {}: requested type {} does not match account type {}";
    public static final String LOG_WITHDRAWAL_PROCESSING = "Processing withdrawal of {} from {} account {}";
    public static final String LOG_DEPOSIT_REJECTED_ACCOUNT_NUMBER = "Deposit rejected: missing/malformed account number";
    public static final String LOG_DEPOSIT_REJECTED_AMOUNT = "Deposit rejected for account {}: amount must be positive, got {}";
    public static final String LOG_DEPOSIT_REJECTED_PREFIX = "Deposit rejected: unrecognized account number prefix {}";
    public static final String LOG_DEPOSIT_REJECTED_TYPE_MISMATCH = "Deposit rejected for account {}: requested type {} does not match account type {}";
    public static final String LOG_DEPOSIT_REJECTED_TYPE_INVALID = "Deposit rejected for account {}: invalid deposit type {}";
    public static final String LOG_DEPOSIT_PROCESSING = "Processing {} deposit of {} into {} account {}";

    // BankingExceptionHandler log messages
    public static final String LOG_HANDLER_ACCOUNT_NOT_FOUND = "Account not found: {}";
    public static final String LOG_HANDLER_INSUFFICIENT_FUNDS = "Insufficient funds: {}";
    public static final String LOG_HANDLER_INVALID_REQUEST = "Invalid banking request: {}";

    // NotificationService log messages
    public static final String LOG_EMAIL_SENT = "Email sent to {} on thread: {}";
    public static final String LOG_EMAIL_INTERRUPTED = "Email send to {} interrupted";
    public static final String LOG_REPORT_INTERRUPTED = "Report generation interrupted";
    public static final String LOG_REPORT_GENERATED = "Report generated successfully";

    // PaymentService log messages
    public static final String LOG_PAYMENT_PROCESSING = "Processing payment via BankClient";
    public static final String LOG_PAYMENT_FALLBACK_TRIGGERED = "Payment circuit breaker fallback triggered: {}";

    // CustomerService log messages
    public static final String LOG_CUSTOMER_SAMPLE_RETURNED = "Returning sample customer record";

    // Shared domain/request validation messages (Address, AccountRegistrationRequest,
    // WithdrawalRequest, WithdrawalForm, DepositForm)
    public static final String VALIDATION_STREET_REQUIRED = "Street address is required";
    public static final String VALIDATION_CITY_REQUIRED = "City is required";
    public static final String VALIDATION_CITY_INVALID_CHARS = "Invalid characters in city name";
    public static final String VALIDATION_STATE_REQUIRED = "State is required";
    public static final String VALIDATION_STATE_LENGTH = "State must be exactly 2 characters (e.g., TX)";
    public static final String VALIDATION_ZIP_REQUIRED = "Zip code is required";

    // AccountRegistrationRequest-specific validation messages
    public static final String VALIDATION_FIRST_NAME_REQUIRED = "First name is required";
    public static final String VALIDATION_FIRST_NAME_MAX_LENGTH = "First name cannot exceed 50 characters";
    public static final String VALIDATION_LAST_NAME_REQUIRED = "Last name is required";
    public static final String VALIDATION_LAST_NAME_MAX_LENGTH = "Last name cannot exceed 50 characters";
    public static final String VALIDATION_DOB_REQUIRED = "Date of birth is required";
    public static final String VALIDATION_ADDRESS_LINE1_REQUIRED = "Address line1 is required";
    public static final String VALIDATION_ACCOUNT_TYPE_REQUIRED = "Account type is required";
    public static final String VALIDATION_DOB_YEAR_MIN = "Year must be 1940 or later";

    // Address-specific validation messages
    public static final String VALIDATION_ZIP_NON_NEGATIVE = "Non negative numbers";

    // DepositForm-specific validation messages
    public static final String VALIDATION_DEPOSIT_ADDRESS_LINE1_REQUIRED = "address line1 required.";
    public static final String VALIDATION_DEPOSIT_ADDRESS_LINE1_INVALID_CHARS = "Invalid characters in address line1";
    public static final String VALIDATION_DEPOSIT_ZIP_PATTERN = "zip code must be exactly 5 digits";

    // Customer-specific validation messages
    public static final String VALIDATION_DOB_YEAR_1940 = "Date of birth must be from year 1940 onwards";

    // "Letters only" name-pattern messages. Preserved verbatim per original call site
    // (inconsistent punctuation predates this centralization) rather than merged, to
    // avoid silently changing validation text.
    public static final String VALIDATION_NAME_LETTERS_ONLY_SHORT = "no special chars"; // Customer.firstName
    public static final String VALIDATION_NAME_LETTERS_ONLY_PROPER = "matches one or more letters, no spaces, no special chars"; // Customer.lastName
    public static final String VALIDATION_NAME_LETTERS_ONLY_NO_COMMA = "matches one or more letters, no spaces no special chars"; // DepositForm.firstName, WithdrawalRequest.firstName
    public static final String VALIDATION_NAME_LETTERS_ONLY_SPACE_COMMA = "matches one or more letters, no spaces ,no special chars"; // DepositForm.lastName, WithdrawalRequest.lastName, WithdrawalForm.lastName
    public static final String VALIDATION_NAME_LETTERS_ONLY_COMMA_NO_SPACE = "matches one or more letters, no spaces,no special chars"; // WithdrawalForm.firstName
}
