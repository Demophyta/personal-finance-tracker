package com.finance.tracker.dto;

import java.math.BigDecimal;

public record BudgetRequestDTO(

        Long categoryId,

        BigDecimal amount,

        int month,

        int year

) {
}