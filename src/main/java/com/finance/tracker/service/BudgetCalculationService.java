package com.finance.tracker.service;


import com.finance.tracker.dto.BudgetStatus;
import com.finance.tracker.dto.BudgetUsageDTO;
import com.finance.tracker.model.Budget;

public interface BudgetCalculationService {

    BudgetUsageDTO calculateUsage(Budget budget);

    BudgetStatus calculateStatus(Budget budget);

}