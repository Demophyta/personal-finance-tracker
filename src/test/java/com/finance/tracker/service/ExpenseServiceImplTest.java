package com.finance.tracker.service;

import com.finance.tracker.dto.ExpenseRequestDTO;
import com.finance.tracker.dto.ExpenseResponseDTO;
import com.finance.tracker.exception.InsufficientBalanceException;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.Expense;
import com.finance.tracker.model.TransactionType;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetCalculationService budgetCalculationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    void shouldCreateExpenseSuccessfully() {

        // Arrange
        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setUserId(userId);
        user.setEmail("test@gmail.com");

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Food");

        ExpenseRequestDTO request = new ExpenseRequestDTO(
                new BigDecimal("5000.00"),
                LocalDate.of(2026, 8, 19),
                "Lunch",
                TransactionType.EXPENSE,
                categoryId
        );

        Expense savedExpense = Expense.builder()
                .expenseId(1L)
                .amount(new BigDecimal("5000.00"))
                .date(request.date())
                .description("Lunch")
                .type(TransactionType.EXPENSE)
                .user(user)
                .category(category)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(balanceService.getCurrentBalance(userId))
                .thenReturn(new BigDecimal("50000.00"));

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(savedExpense);

        when(budgetRepository.findActiveBudget(
                user,
                category,
                savedExpense.getDate()
        )).thenReturn(Optional.empty());

        // Act
        ExpenseResponseDTO result =
                expenseService.createExpense(request, userId);

        // Assert
        assertNotNull(result);

        assertEquals(
                1L,
                result.expenseId()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                result.amount()
        );

        assertEquals(
                "Lunch",
                result.description()
        );

        assertEquals(
                TransactionType.EXPENSE,
                result.type()
        );

        assertEquals(
                "Food",
                result.category()
        );

        verify(userRepository)
                .findById(userId);

        verify(categoryRepository)
                .findById(categoryId);

        verify(balanceService)
                .getCurrentBalance(userId);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(notificationService).createNotification(
                eq(user),
                contains(
                        "Expense of ₦5000.00 recorded under Food."
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {

        // Arrange
        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Food");

        ExpenseRequestDTO request = new ExpenseRequestDTO(
                new BigDecimal("50000.00"),
                LocalDate.of(2026, 8, 19),
                "Expensive meal",
                TransactionType.EXPENSE,
                categoryId
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(balanceService.getCurrentBalance(userId))
                .thenReturn(new BigDecimal("10000.00"));

        // Act & Assert
        InsufficientBalanceException exception =
                assertThrows(
                        InsufficientBalanceException.class,
                        () -> expenseService.createExpense(
                                request,
                                userId
                        )
                );

        assertEquals(
                "Insufficient balance. Current balance is ₦10000.00",
                exception.getMessage()
        );

        verify(expenseRepository, never())
                .save(any(Expense.class));

        verifyNoInteractions(notificationService);
    }
}