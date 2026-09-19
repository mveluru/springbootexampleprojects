package org.bee.banking.exception;

public class MinBalanceException extends RuntimeException {
    public MinBalanceException(String message) {
        super(message);
    }
}
