package com.finance.tracker.service;

import com.finance.tracker.dto.DashboardResponseDTO;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.model.Budget;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.Expense;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.BudgetRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;


    @Test
    void shouldGetDashboardSuccessfully() {

        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);

        Category food = new Category();
        food.setCategoryId(1L);
        food.setName("Food");

        Category transport = new Category();
        transport.setCategoryId(2L);
        transport.setName("Transport");

        Expense foodExpense = Expense.builder()
                .expenseId(1L)
                .amount(new BigDecimal("10000.00"))
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .category(food)
                .user(user)
                .build();

        Expense transportExpense = Expense.builder()
                .expenseId(2L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .category(transport)
                .user(user)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(expenseRepository.findByUserAndTypeAndDateBetween(
                eq(user),
                eq(TransactionType.EXPENSE),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of(
                foodExpense,
                transportExpense
        ));

        when(balanceService.getTotalIncome(userId))
                .thenReturn(new BigDecimal("100000.00"));

        when(balanceService.getTotalExpenses(userId))
                .thenReturn(new BigDecimal("15000.00"));

        when(balanceService.getCurrentBalance(userId))
                .thenReturn(new BigDecimal("85000.00"));

        Budget budget = Budget.builder()
                .amount(new BigDecimal("50000.00"))
                .build();

        when(budgetRepository.findActiveBudgets(
                eq(user),
                any(LocalDate.class)
        )).thenReturn(List.of(budget));

        DashboardResponseDTO result =
                dashboardService.getDashboard(userId);

        // Basic result
        assertNotNull(result);

        // Financial summary
        assertEquals(
                new BigDecimal("100000.00"),
                result.totalIncome()
        );

        assertEquals(
                new BigDecimal("15000.00"),
                result.totalExpenses()
        );

        assertEquals(
                new BigDecimal("85000.00"),
                result.netBalance()
        );

        // Budget
        assertEquals(
                new BigDecimal("35000.00"),
                result.remainingBudget()
        );

        // Categories
        assertNotNull(result.topCategories());

        assertEquals(
                2,
                result.topCategories().size()
        );

        assertEquals(
                "Food",
                result.topCategories().get(0).categoryName()
        );

        assertEquals(
                new BigDecimal("10000.00"),
                result.topCategories().get(0).totalSpent()
        );
    }


    @Test
    void shouldReturnOnlyTopThreeCategories() {

        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);

        Category food = createCategory("Food");
        Category transport = createCategory("Transport");
        Category shopping = createCategory("Shopping");
        Category bills = createCategory("Bills");

        Expense foodExpense =
                createExpense(user, food, "10000.00");

        Expense transportExpense =
                createExpense(user, transport, "9000.00");

        Expense shoppingExpense =
                createExpense(user, shopping, "8000.00");

        Expense billsExpense =
                createExpense(user, bills, "7000.00");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(expenseRepository.findByUserAndTypeAndDateBetween(
                eq(user),
                eq(TransactionType.EXPENSE),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of(
                foodExpense,
                transportExpense,
                shoppingExpense,
                billsExpense
        ));

        when(balanceService.getTotalIncome(userId))
                .thenReturn(new BigDecimal("100000.00"));

        when(balanceService.getTotalExpenses(userId))
                .thenReturn(new BigDecimal("34000.00"));

        when(balanceService.getCurrentBalance(userId))
                .thenReturn(new BigDecimal("66000.00"));

        when(budgetRepository.findActiveBudgets(
                eq(user),
                any(LocalDate.class)
        )).thenReturn(List.of());

        DashboardResponseDTO result =
                dashboardService.getDashboard(userId);

        assertEquals(
                3,
                result.topCategories().size()
        );

        assertEquals(
                "Food",
                result.topCategories().get(0).categoryName()
        );

        assertEquals(
                "Transport",
                result.topCategories().get(1).categoryName()
        );

        assertEquals(
                "Shopping",
                result.topCategories().get(2).categoryName()
        );
    }


    @Test
    void shouldIgnoreExpenseWithoutCategory() {

        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);

        Expense expense = Expense.builder()
                .expenseId(1L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .category(null)
                .user(user)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(expenseRepository.findByUserAndTypeAndDateBetween(
                eq(user),
                eq(TransactionType.EXPENSE),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of(expense));

        when(balanceService.getTotalIncome(userId))
                .thenReturn(BigDecimal.ZERO);

        when(balanceService.getTotalExpenses(userId))
                .thenReturn(new BigDecimal("5000.00"));

        when(balanceService.getCurrentBalance(userId))
                .thenReturn(new BigDecimal("-5000.00"));

        when(budgetRepository.findActiveBudgets(
                eq(user),
                any(LocalDate.class)
        )).thenReturn(List.of());

        DashboardResponseDTO result =
                dashboardService.getDashboard(userId);

        assertNotNull(result.topCategories());
        assertTrue(result.topCategories().isEmpty());
    }


    @Test
    void shouldReturnZeroWhenBudgetIsExceeded() {

        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(expenseRepository.findByUserAndTypeAndDateBetween(
                eq(user),
                eq(TransactionType.EXPENSE),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of());

        when(balanceService.getTotalIncome(userId))
                .thenReturn(new BigDecimal("50000.00"));

        when(balanceService.getTotalExpenses(userId))
                .thenReturn(new BigDecimal("60000.00"));

        when(balanceService.getCurrentBalance(userId))
                .thenReturn(new BigDecimal("-10000.00"));

        Budget budget = Budget.builder()
                .amount(new BigDecimal("30000.00"))
                .build();

        when(budgetRepository.findActiveBudgets(
                eq(user),
                any(LocalDate.class)
        )).thenReturn(List.of(budget));

        DashboardResponseDTO result =
                dashboardService.getDashboard(userId);

        assertEquals(
                BigDecimal.ZERO,
                result.remainingBudget()
        );
    }


    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        Long userId = 99L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> dashboardService.getDashboard(userId)
        );

        verify(expenseRepository, never())
                .findByUserAndTypeAndDateBetween(
                        any(),
                        any(),
                        any(),
                        any()
                );

        verify(balanceService, never())
                .getTotalIncome(anyLong());

        verify(budgetRepository, never())
                .findActiveBudgets(any(), any());
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private Category createCategory(String name) {

        Category category = new Category();
        category.setName(name);

        return category;
    }


    private Expense createExpense(
            User user,
            Category category,
            String amount) {

        return Expense.builder()
                .amount(new BigDecimal(amount))
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .category(category)
                .user(user)
                .build();
    }
}