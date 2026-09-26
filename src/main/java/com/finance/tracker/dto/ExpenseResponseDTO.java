package com.finance.tracker.dto;

import com.finance.tracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponseDTO(

        Long expenseId,

        BigDecimal amount,

        LocalDate date,

        String description,

        TransactionType type,

        String category,

        LocalDateTime createdAt

) {
}