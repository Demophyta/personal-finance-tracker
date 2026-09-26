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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;


    // =========================================================
    // CREATE BUDGET
    // =========================================================

    @Test
    void shouldCreateBudgetSuccessfully() {

        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Food");

        BudgetRequestDTO request = new BudgetRequestDTO(
                categoryId,
                new BigDecimal("50000.00"),
                8,
                2026
        );

        Budget savedBudget = Budget.builder()
                .budgetId(1L)
                .user(user)
                .category(category)
                .amount(new BigDecimal("50000.00"))
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(budgetRepository.findActiveBudget(
                user,
                category,
                LocalDate.of(2026, 8, 1)
        )).thenReturn(Optional.empty());

        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(savedBudget);

        // Act
        BudgetResponseDTO result =
                budgetService.createBudget(userId, request);

        // Assert
        assertNotNull(result);

        assertEquals(1L, result.getBudgetId());

        assertEquals(
                "Food",
                result.getCategoryName()
        );

        assertEquals(
                new BigDecimal("50000.00"),
                result.getAmount()
        );

        assertEquals(8, result.getMonth());

        assertEquals(2026, result.getYear());

        assertEquals(
                LocalDate.of(2026, 8, 1),
                result.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 8, 31),
                result.getEndDate()
        );

        verify(budgetRepository)
                .save(any(Budget.class));
    }


    // =========================================================
    // CREATE - DUPLICATE BUDGET
    // =========================================================

    @Test
    void shouldRejectDuplicateBudget() {

        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Food");

        BudgetRequestDTO request = new BudgetRequestDTO(
                categoryId,
                new BigDecimal("50000.00"),
                8,
                2026
        );

        Budget existingBudget = Budget.builder()
                .budgetId(1L)
                .user(user)
                .category(category)
                .amount(new BigDecimal("40000.00"))
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(budgetRepository.findActiveBudget(
                user,
                category,
                LocalDate.of(2026, 8, 1)
        )).thenReturn(Optional.of(existingBudget));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> budgetService.createBudget(userId, request)
        );

        assertEquals(
                "Budget already exists for this category and month.",
                exception.getMessage()
        );

        verify(budgetRepository, never())
                .save(any(Budget.class));
    }


    // =========================================================
    // UPDATE BUDGET
    // =========================================================

    @Test
    void shouldUpdateBudgetSuccessfully() {

        Long budgetId = 1L;
        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Transport");

        Budget budget = Budget.builder()
                .budgetId(budgetId)
                .user(user)
                .category(category)
                .amount(new BigDecimal("30000.00"))
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 31))
                .build();

        BudgetRequestDTO request = new BudgetRequestDTO(
                categoryId,
                new BigDecimal("40000.00"),
                8,
                2026
        );

        when(budgetRepository.findById(budgetId))
                .thenReturn(Optional.of(budget));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(budgetRepository.save(budget))
                .thenReturn(budget);

        // Act
        BudgetResponseDTO result =
                budgetService.updateBudget(
                        budgetId,
                        userId,
                        request
                );

        // Assert
        assertNotNull(result);

        assertEquals(
                new BigDecimal("40000.00"),
                result.getAmount()
        );

        assertEquals(8, result.getMonth());

        assertEquals(2026, result.getYear());

        assertEquals(
                LocalDate.of(2026, 8, 1),
                result.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 8, 31),
                result.getEndDate()
        );

        verify(budgetRepository)
                .save(budget);
    }


    // =========================================================
    // UPDATE - UNAUTHORIZED
    // =========================================================

    @Test
    void shouldRejectUnauthorizedBudgetUpdate() {

        Long budgetId = 1L;

        User owner = new User();
        owner.setUserId(1L);

        Budget budget = Budget.builder()
                .budgetId(budgetId)
                .user(owner)
                .build();

        when(budgetRepository.findById(budgetId))
                .thenReturn(Optional.of(budget));

        BudgetRequestDTO request = new BudgetRequestDTO(
                10L,
                new BigDecimal("50000.00"),
                8,
                2026
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> budgetService.updateBudget(
                        budgetId,
                        2L,
                        request
                )
        );

        assertEquals(
                "Unauthorized",
                exception.getMessage()
        );

        verify(budgetRepository, never())
                .save(any(Budget.class));
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldDeleteBudgetSuccessfully() {

        Long budgetId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);

        Budget budget = Budget.builder()
                .budgetId(budgetId)
                .user(user)
                .build();

        when(budgetRepository.findById(budgetId))
                .thenReturn(Optional.of(budget));

        // Act
        budgetService.deleteBudget(
                budgetId,
                userId
        );

        // Assert
        verify(budgetRepository)
                .delete(budget);
    }


    // =========================================================
    // DELETE - UNAUTHORIZED
    // =========================================================

    @Test
    void shouldRejectUnauthorizedBudgetDelete() {

        Long budgetId = 1L;

        User owner = new User();
        owner.setUserId(1L);

        Budget budget = Budget.builder()
                .budgetId(budgetId)
                .user(owner)
                .build();

        when(budgetRepository.findById(budgetId))
                .thenReturn(Optional.of(budget));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> budgetService.deleteBudget(
                        budgetId,
                        2L
                )
        );

        assertEquals(
                "Unauthorized",
                exception.getMessage()
        );

        verify(budgetRepository, never())
                .delete(any(Budget.class));
    }


    // =========================================================
    // GET USER BUDGETS
    // =========================================================

    @Test
    void shouldGetUserBudgets() {

        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(10L);
        category.setName("Food");

        Budget budget = Budget.builder()
                .budgetId(1L)
                .user(user)
                .category(category)
                .amount(new BigDecimal("50000.00"))
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .build();

        when(budgetRepository.findByUser_UserId(userId))
                .thenReturn(List.of(budget));

        // Act
        List<BudgetResponseDTO> result =
                budgetService.getUserBudgets(userId);

        // Assert
        assertEquals(1, result.size());

        BudgetResponseDTO response = result.get(0);

        assertEquals(
                1L,
                response.getBudgetId()
        );

        assertEquals(
                "Food",
                response.getCategoryName()
        );

        assertEquals(
                new BigDecimal("50000.00"),
                response.getAmount()
        );
    }


    // =========================================================
    // BUDGET USAGE
    // =========================================================

    @Test
    void shouldCalculateBudgetUsage() {

        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Food");

        Budget budget = Budget.builder()
                .budgetId(1L)
                .user(user)
                .category(category)
                .amount(new BigDecimal("50000.00"))
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .build();

        when(budgetRepository.findByUser_UserId(userId))
                .thenReturn(List.of(budget));

        when(expenseRepository.sumByUserIdAndCategoryAndDateBetween(
                userId,
                categoryId,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        )).thenReturn(new BigDecimal("12500.00"));

        // Act
        List<BudgetUsageDTO> result =
                budgetService.getBudgetUsage(userId);

        // Assert
        assertEquals(1, result.size());

        BudgetUsageDTO usage = result.get(0);

        assertEquals(
                new BigDecimal("50000.00"),
                usage.getBudgetAmount()
        );

        assertEquals(
                new BigDecimal("12500.00"),
                usage.getSpentAmount()
        );

        assertEquals(
                new BigDecimal("37500.00"),
                usage.getRemainingAmount()
        );

        assertEquals(
                25.0,
                usage.getUsagePercentage()
        );

        assertEquals(
                "Food",
                usage.getCategory()
        );

        assertEquals(
                LocalDate.of(2026, 8, 1),
                usage.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 8, 31),
                usage.getEndDate()
        );
    }


    // =========================================================
    // BUDGET USAGE - NO EXPENSES
    // =========================================================

    @Test
    void shouldReturnZeroUsageWhenNoExpenses() {

        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Food");

        Budget budget = Budget.builder()
                .budgetId(1L)
                .user(user)
                .category(category)
                .amount(new BigDecimal("50000.00"))
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .build();

        when(budgetRepository.findByUser_UserId(userId))
                .thenReturn(List.of(budget));

        when(expenseRepository.sumByUserIdAndCategoryAndDateBetween(
                userId,
                categoryId,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        )).thenReturn(null);

        // Act
        List<BudgetUsageDTO> result =
                budgetService.getBudgetUsage(userId);

        // Assert
        assertEquals(1, result.size());

        BudgetUsageDTO usage = result.get(0);

        assertEquals(
                BigDecimal.ZERO,
                usage.getSpentAmount()
        );

        assertEquals(
                new BigDecimal("50000.00"),
                usage.getRemainingAmount()
        );

        assertEquals(
                0.0,
                usage.getUsagePercentage()
        );
    }
}