package com.finance.tracker.dto;

import com.finance.tracker.model.Frequency;
import com.finance.tracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionResponseDTO(
        Long recurringTransactionId,
        String categoryName,
        BigDecimal amount,
        TransactionType type,
        Frequency frequency,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
}