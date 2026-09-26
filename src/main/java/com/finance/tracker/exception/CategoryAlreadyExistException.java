package com.finance.tracker.exception;

public class CategoryAlreadyExistException extends RuntimeException {

    public CategoryAlreadyExistException() {
        super("Category already exists for this user.");
    }
}