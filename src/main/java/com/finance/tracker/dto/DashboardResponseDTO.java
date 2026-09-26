package com.finance.tracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDTO(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        List<CategorySummaryDTO> topCategories,
        BigDecimal remainingBudget
) {}
