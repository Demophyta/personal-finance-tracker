package com.finance.tracker.service;

import com.finance.tracker.dto.CategoryExpenseDTO;
import com.finance.tracker.dto.ExpenseRequestDTO;
import com.finance.tracker.dto.ExpenseResponseDTO;
import com.finance.tracker.dto.ExpenseSummaryDTO;
import com.finance.tracker.dto.YearlyExpenseDTO;

import java.util.List;

public interface ExpenseService {

    /**
     * Creates a new expense for a user.
     */
    ExpenseResponseDTO createExpense(
            ExpenseRequestDTO request,
            Long userId
    );

    /**
     * Returns all expenses for a user.
     */
    List<ExpenseResponseDTO> getUserExpenses(Long userId);

    /**
     * Monthly summary.
     */
    ExpenseSummaryDTO getMonthlySummary(
            Long userId,
            int year,
            int month
    );

    /**
     * Category report.
     */
    List<CategoryExpenseDTO> getCategoryReport(
            Long userId,
            int year,
            int month
    );

    /**
     * Yearly report.
     */
    YearlyExpenseDTO getYearlyReport(
            Long userId,
            int year
    );
}