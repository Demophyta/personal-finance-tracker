package com.finance.tracker.dto;

import com.finance.tracker.model.Frequency;
import com.finance.tracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionRequestDTO(
        Long categoryId,
        BigDecimal amount,
        TransactionType type,       // INCOME or EXPENSE
        Frequency frequency,   // DAILY, WEEKLY, MONTHLY
        LocalDate startDate,
        LocalDate endDate
) {}
