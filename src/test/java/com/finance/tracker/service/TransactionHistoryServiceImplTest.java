package com.finance.tracker.service;

import com.finance.tracker.dto.TransactionHistoryDTO;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.Expense;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
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
class TransactionHistoryServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionHistoryServiceImpl transactionHistoryService;


    // =========================================================
    // GET USER TRANSACTIONS
    // =========================================================

    @Test
    void shouldGetUserTransactions() {

        Long userId = 1L;

        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(1L);
        category.setName("Food");

        Expense expense = Expense.builder()
                .expenseId(10L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2026, 8, 15))
                .description("Lunch")
                .type(TransactionType.EXPENSE)
                .category(category)
                .user(user)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(expenseRepository
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        start,
                        end
                ))
                .thenReturn(List.of(expense));

        List<TransactionHistoryDTO> result =
                transactionHistoryService.getUserTransactions(
                        userId,
                        start,
                        end
                );

        assertEquals(1, result.size());

        TransactionHistoryDTO dto = result.get(0);

        assertEquals(10L, dto.id());
        assertEquals("Food", dto.categoryName());

        assertEquals(
                new BigDecimal("5000.00"),
                dto.amount()
        );

        assertEquals("EXPENSE", dto.type());
        assertEquals("Lunch", dto.description());

        assertEquals(
                LocalDate.of(2026, 8, 15),
                dto.date()
        );

        verify(userRepository)
                .findById(userId);

        verify(expenseRepository)
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        start,
                        end
                );
    }


    // =========================================================
    // INCOME WITHOUT CATEGORY
    // =========================================================

    @Test
    void shouldMapIncomeWithoutCategory() {

        Long userId = 1L;

        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        User user = new User();
        user.setUserId(userId);

        Expense income = Expense.builder()
                .expenseId(20L)
                .amount(new BigDecimal("100000.00"))
                .date(LocalDate.of(2026, 8, 10))
                .description("Salary")
                .type(TransactionType.INCOME)
                .category(null)
                .user(user)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(expenseRepository
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        start,
                        end
                ))
                .thenReturn(List.of(income));

        List<TransactionHistoryDTO> result =
                transactionHistoryService.getUserTransactions(
                        userId,
                        start,
                        end
                );

        assertEquals(1, result.size());

        TransactionHistoryDTO dto = result.get(0);

        assertEquals("Income", dto.categoryName());
        assertEquals("INCOME", dto.type());

        assertEquals(
                new BigDecimal("100000.00"),
                dto.amount()
        );

        assertEquals("Salary", dto.description());
    }


    // =========================================================
    // USER NOT FOUND
    // =========================================================

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        Long userId = 99L;

        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> transactionHistoryService.getUserTransactions(
                        userId,
                        start,
                        end
                )
        );

        verify(expenseRepository, never())
                .findByUserAndDateBetweenOrderByDateDesc(
                        any(),
                        any(),
                        any()
                );
    }


    // =========================================================
    // NO TRANSACTIONS
    // =========================================================

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist() {

        Long userId = 1L;

        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        User user = new User();
        user.setUserId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(expenseRepository
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        start,
                        end
                ))
                .thenReturn(List.of());

        List<TransactionHistoryDTO> result =
                transactionHistoryService.getUserTransactions(
                        userId,
                        start,
                        end
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(expenseRepository)
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        start,
                        end
                );
    }
}