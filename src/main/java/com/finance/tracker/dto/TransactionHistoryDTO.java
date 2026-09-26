package com.finance.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionHistoryDTO(
        Long id,
        String categoryName,
        BigDecimal amount,
        String type,
        String description,
        LocalDate date
) {}
