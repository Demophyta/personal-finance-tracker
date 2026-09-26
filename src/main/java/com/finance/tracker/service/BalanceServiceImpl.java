package com.finance.tracker.service;

import com.finance.tracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final ExpenseRepository expenseRepository;

    @Override
    public BigDecimal getTotalIncome(Long userId) {
        return expenseRepository.getTotalIncome(userId);
    }

    @Override
    public BigDecimal getTotalExpenses(Long userId) {
        return expenseRepository.getTotalExpenses(userId);
    }

    @Override
    public BigDecimal getCurrentBalance(Long userId) {

        BigDecimal income = getTotalIncome(userId);
        BigDecimal expenses = getTotalExpenses(userId);

        return income.subtract(expenses);
    }

    @Override
    public BigDecimal getMonthlyIncome(Long userId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return expenseRepository.getMonthlyIncome(
                userId,
                start,
                end
        );
    }

    @Override
    public BigDecimal getMonthlyExpenses(Long userId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return expenseRepository.getMonthlyExpenses(
                userId,
                start,
                end
        );
    }

    @Override
    public BigDecimal getMonthlyBalance(Long userId, int year, int month) {

        BigDecimal income = getMonthlyIncome(userId, year, month);
        BigDecimal expenses = getMonthlyExpenses(userId, year, month);

        return income.subtract(expenses);
    }
}