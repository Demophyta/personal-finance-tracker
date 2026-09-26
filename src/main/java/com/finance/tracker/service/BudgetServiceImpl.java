package com.finance.tracker.service;

import com.finance.tracker.dto.BudgetRequestDTO;
import com.finance.tracker.dto.BudgetResponseDTO;
import com.finance.tracker.dto.BudgetUsageDTO;
import com.finance.tracker.model.Budget;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public BudgetResponseDTO createBudget(Long userId, BudgetRequestDTO requestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(requestDTO.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        LocalDate startDate = LocalDate.of(
                requestDTO.year(),
                requestDTO.month(),
                1
        );

        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        budgetRepository
                .findActiveBudget(user, category, startDate)
                .ifPresent(b -> {
                    throw new RuntimeException(
                            "Budget already exists for this category and month."
                    );
                });

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .amount(requestDTO.amount())
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return mapToResponseDTO(
                budgetRepository.save(budget)
        );
    }

    @Override
    public BudgetResponseDTO updateBudget(
            Long budgetId,
            Long userId,
            BudgetRequestDTO requestDTO) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        Category category = categoryRepository.findById(requestDTO.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        LocalDate startDate = LocalDate.of(
                requestDTO.year(),
                requestDTO.month(),
                1
        );

        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        budget.setCategory(category);
        budget.setAmount(requestDTO.amount());
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);

        return mapToResponseDTO(
                budgetRepository.save(budget)
        );
    }

    @Override
    public void deleteBudget(Long budgetId, Long userId) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        budgetRepository.delete(budget);
    }

    @Override
    public List<BudgetResponseDTO> getUserBudgets(Long userId) {

        return budgetRepository.findByUser_UserId(userId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<BudgetUsageDTO> getBudgetUsage(Long userId) {

        List<Budget> budgets = budgetRepository.findByUser_UserId(userId);

        return budgets.stream()
                .map(budget -> {

                    BigDecimal spent = expenseRepository
                            .sumByUserIdAndCategoryAndDateBetween(
                                    userId,
                                    budget.getCategory().getCategoryId(),
                                    budget.getStartDate(),
                                    budget.getEndDate()
                            );

                    if (spent == null) {
                        spent = BigDecimal.ZERO;
                    }

                    BigDecimal remaining = budget.getAmount().subtract(spent);

                    double usagePercentage = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                            ? spent.divide(
                                    budget.getAmount(),
                                    4,
                                    RoundingMode.HALF_UP
                            )
                            .multiply(BigDecimal.valueOf(100))
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
                })
                .toList();
    }
    private BudgetResponseDTO mapToResponseDTO(Budget budget) {

        return new BudgetResponseDTO(
                budget.getBudgetId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                budget.getStartDate().getMonthValue(),
                budget.getStartDate().getYear(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }

}