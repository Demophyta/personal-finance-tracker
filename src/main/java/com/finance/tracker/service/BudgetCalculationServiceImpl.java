package com.finance.tracker.service;

import com.finance.tracker.dto.BudgetStatus;
import com.finance.tracker.dto.BudgetUsageDTO;
import com.finance.tracker.model.Budget;
import com.finance.tracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BudgetCalculationServiceImpl implements BudgetCalculationService {

    private final ExpenseRepository expenseRepository;

    @Override
    public BudgetUsageDTO calculateUsage(Budget budget) {

        BigDecimal spent = expenseRepository.sumByUserIdAndCategoryAndDateBetween(
                budget.getUser().getUserId(),
                budget.getCategory().getCategoryId(),
                budget.getStartDate(),
                budget.getEndDate()
        );

        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        // Allow negative remaining amount if budget is exceeded
        BigDecimal remaining = budget.getAmount().subtract(spent);

        double usagePercentage = BigDecimal.ZERO.compareTo(budget.getAmount()) < 0
                ? spent.divide(
                        budget.getAmount(),
                        4,
                        RoundingMode.HALF_UP
                ).multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return new BudgetUsageDTO(
                budget.getBudgetId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                spent,
                remaining,
                usagePercentage,
                budget.getStartDate(),
                budget.getEndDate()
        );
    }
    @Override
    public BudgetStatus calculateStatus(Budget budget) {

        BudgetUsageDTO usage = calculateUsage(budget);

        boolean warning =
                usage.getUsagePercentage() >= 80
                        && usage.getUsagePercentage() < 100;

        boolean exceeded =
                usage.getUsagePercentage() >= 100;

        return new BudgetStatus(
                usage,
                warning,
                exceeded
        );
    }
}