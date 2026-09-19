package org.bee.banking.exception;

public class StatementRangeExceededException extends RuntimeException {
    public StatementRangeExceededException(String message) {
        super(message);
    }
}
