package com.finance.tracker.service;

import com.finance.tracker.dto.BudgetRequestDTO;
import com.finance.tracker.dto.BudgetResponseDTO;
import com.finance.tracker.dto.BudgetUsageDTO;

import java.util.List;

public interface BudgetService {
    BudgetResponseDTO createBudget(Long userId, BudgetRequestDTO requestDTO);
    BudgetResponseDTO updateBudget(Long budgetId, Long userId, BudgetRequestDTO requestDTO);
    void deleteBudget(Long budgetId, Long userId);
    List<BudgetResponseDTO> getUserBudgets(Long userId);
    List<BudgetUsageDTO> getBudgetUsage(Long userId);
}
