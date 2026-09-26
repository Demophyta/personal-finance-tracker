package com.finance.tracker.service;

import java.math.BigDecimal;

public interface BalanceService {

    /**
     * Total income recorded.
     */
    BigDecimal getTotalIncome(Long userId);

    /**
     * Total expenses recorded.
     */
    BigDecimal getTotalExpenses(Long userId);

    /**
     * Current available balance.
     */
    BigDecimal getCurrentBalance(Long userId);

    /**
     * Monthly income.
     */
    BigDecimal getMonthlyIncome(Long userId, int year, int month);

    /**
     * Monthly expenses.
     */
    BigDecimal getMonthlyExpenses(Long userId, int year, int month);

    /**
     * Monthly balance.
     */
    BigDecimal getMonthlyBalance(Long userId, int year, int month);
}