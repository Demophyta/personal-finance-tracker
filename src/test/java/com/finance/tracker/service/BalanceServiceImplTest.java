package com.finance.tracker.service;

import com.finance.tracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BalanceServiceImpl balanceService;


    // =========================================================
    // TOTAL INCOME
    // =========================================================

    @Test
    void shouldGetTotalIncome() {

        Long userId = 1L;

        when(expenseRepository.getTotalIncome(userId))
                .thenReturn(new BigDecimal("100000.00"));

        BigDecimal result =
                balanceService.getTotalIncome(userId);

        assertEquals(
                new BigDecimal("100000.00"),
                result
        );

        verify(expenseRepository)
                .getTotalIncome(userId);
    }


    // =========================================================
    // TOTAL EXPENSES
    // =========================================================

    @Test
    void shouldGetTotalExpenses() {

        Long userId = 1L;

        when(expenseRepository.getTotalExpenses(userId))
                .thenReturn(new BigDecimal("40000.00"));

        BigDecimal result =
                balanceService.getTotalExpenses(userId);

        assertEquals(
                new BigDecimal("40000.00"),
                result
        );

        verify(expenseRepository)
                .getTotalExpenses(userId);
    }


    // =========================================================
    // CURRENT BALANCE
    // =========================================================

    @Test
    void shouldCalculateCurrentBalance() {

        Long userId = 1L;

        when(expenseRepository.getTotalIncome(userId))
                .thenReturn(new BigDecimal("100000.00"));

        when(expenseRepository.getTotalExpenses(userId))
                .thenReturn(new BigDecimal("40000.00"));

        BigDecimal result =
                balanceService.getCurrentBalance(userId);

        assertEquals(
                new BigDecimal("60000.00"),
                result
        );

        verify(expenseRepository)
                .getTotalIncome(userId);

        verify(expenseRepository)
                .getTotalExpenses(userId);
    }


    // =========================================================
    // MONTHLY INCOME
    // =========================================================

    @Test
    void shouldGetMonthlyIncome() {

        Long userId = 1L;
        int year = 2026;
        int month = 8;

        LocalDate start =
                LocalDate.of(2026, 8, 1);

        LocalDate end =
                LocalDate.of(2026, 8, 31);

        when(expenseRepository.getMonthlyIncome(
                userId,
                start,
                end
        )).thenReturn(new BigDecimal("80000.00"));

        BigDecimal result =
                balanceService.getMonthlyIncome(
                        userId,
                        year,
                        month
                );

        assertEquals(
                new BigDecimal("80000.00"),
                result
        );

        verify(expenseRepository)
                .getMonthlyIncome(
                        userId,
                        start,
                        end
                );
    }


    // =========================================================
    // MONTHLY EXPENSES
    // =========================================================

    @Test
    void shouldGetMonthlyExpenses() {

        Long userId = 1L;
        int year = 2026;
        int month = 8;

        LocalDate start =
                LocalDate.of(2026, 8, 1);

        LocalDate end =
                LocalDate.of(2026, 8, 31);

        when(expenseRepository.getMonthlyExpenses(
                userId,
                start,
                end
        )).thenReturn(new BigDecimal("30000.00"));

        BigDecimal result =
                balanceService.getMonthlyExpenses(
                        userId,
                        year,
                        month
                );

        assertEquals(
                new BigDecimal("30000.00"),
                result
        );

        verify(expenseRepository)
                .getMonthlyExpenses(
                        userId,
                        start,
                        end
                );
    }


    // =========================================================
    // MONTHLY BALANCE
    // =========================================================

    @Test
    void shouldCalculateMonthlyBalance() {

        Long userId = 1L;
        int year = 2026;
        int month = 8;

        LocalDate start =
                LocalDate.of(2026, 8, 1);

        LocalDate end =
                LocalDate.of(2026, 8, 31);

        when(expenseRepository.getMonthlyIncome(
                userId,
                start,
                end
        )).thenReturn(new BigDecimal("80000.00"));

        when(expenseRepository.getMonthlyExpenses(
                userId,
                start,
                end
        )).thenReturn(new BigDecimal("30000.00"));

        BigDecimal result =
                balanceService.getMonthlyBalance(
                        userId,
                        year,
                        month
                );

        assertEquals(
                new BigDecimal("50000.00"),
                result
        );

        verify(expenseRepository)
                .getMonthlyIncome(
                        userId,
                        start,
                        end
                );

        verify(expenseRepository)
                .getMonthlyExpenses(
                        userId,
                        start,
                        end
                );
    }
}