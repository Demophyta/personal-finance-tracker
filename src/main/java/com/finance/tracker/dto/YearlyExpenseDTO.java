package com.finance.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearlyExpenseDTO {
    private int year;
    private BigDecimal totalExpenses;
}
