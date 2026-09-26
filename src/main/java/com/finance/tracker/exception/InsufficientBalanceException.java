package com.finance.tracker.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("Insufficient balance.");
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}