package org.bee.banking.exception;

public class MaxDepositAmountException extends RuntimeException {
    public MaxDepositAmountException(String message) {
        super(message);
    }
}
