package com.finance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class BudgetResponseDTO {

    private Long budgetId;

    private String categoryName;

    private BigDecimal amount;

    private int month;

    private int year;

    private LocalDate startDate;

    private LocalDate endDate;
}