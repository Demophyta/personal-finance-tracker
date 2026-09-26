package com.finance.tracker.exception;

public class BudgetNotFoundException extends RuntimeException {

    public BudgetNotFoundException() {
        super("Budget not found.");
    }

    public BudgetNotFoundException(String message) {
        super(message);
    }
}