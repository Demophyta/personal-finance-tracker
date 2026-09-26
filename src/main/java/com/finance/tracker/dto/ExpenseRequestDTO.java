package com.finance.tracker.dto;

import com.finance.tracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequestDTO(

        BigDecimal amount,

        LocalDate date,

        String description,

        TransactionType type,

        Long categoryId

) {
}