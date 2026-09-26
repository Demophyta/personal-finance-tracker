package com.finance.tracker.service;

import com.finance.tracker.dto.CategorySummaryDTO;
import com.finance.tracker.dto.DashboardResponseDTO;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.model.Expense;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;

    @Override
    public DashboardResponseDTO getDashboard(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());

        // Monthly expense transactions
        List<Expense> expenses =
                expenseRepository.findByUserAndTypeAndDateBetween(
                        user,
                        TransactionType.EXPENSE,
                        start,
                        end
                );

        // Financial summary
        BigDecimal totalIncome = balanceService.getTotalIncome(userId);
        BigDecimal totalExpenses = balanceService.getTotalExpenses(userId);
        BigDecimal currentBalance = balanceService.getCurrentBalance(userId);

        // Dashboard widgets
        List<CategorySummaryDTO> topCategories =
                getTopCategories(expenses);

        BigDecimal remainingBudget =
                calculateRemainingBudget(user, totalExpenses, today);

        return new DashboardResponseDTO(
                totalIncome,
                totalExpenses,
                currentBalance,
                topCategories,
                remainingBudget
        );
    }

    /**
     * Returns the top spending categories for the current month.
     */
    private List<CategorySummaryDTO> getTopCategories(List<Expense> expenses) {

        Map<String, BigDecimal> categoryTotals = expenses.stream()

                .filter(expense -> expense.getCategory() != null)

                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory().getName(),

                        Collectors.mapping(
                                Expense::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return categoryTotals.entrySet()

                .stream()

                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))

                .limit(3)

                .map(entry -> new CategorySummaryDTO(
                        entry.getKey(),
                        entry.getValue()
                ))

                .toList();
    }

    /**
     * Calculates the user's remaining budget.
     */
    private BigDecimal calculateRemainingBudget(
            User user,
            BigDecimal totalExpenses,
            LocalDate today) {

        BigDecimal totalBudget = budgetRepository
                .findActiveBudgets(user, today)
                .stream()
                .map(budget -> budget.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBudget = totalBudget.subtract(totalExpenses);

        return remainingBudget.max(BigDecimal.ZERO);
    }
}