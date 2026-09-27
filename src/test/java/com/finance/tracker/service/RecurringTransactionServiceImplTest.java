package com.finance.tracker.service;

import com.finance.tracker.dto.RecurringTransactionRequestDTO;
import com.finance.tracker.dto.RecurringTransactionResponseDTO;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.Expense;
import com.finance.tracker.model.Frequency;
import com.finance.tracker.model.RecurringTransaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.RecurringTransactionRepository;
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
class RecurringTransactionServiceImplTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private RecurringTransactionServiceImpl recurringTransactionService;


    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void shouldCreateRecurringTransaction() {

        Long userId = 1L;
        Long categoryId = 10L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Netflix");

        RecurringTransactionRequestDTO request =
                new RecurringTransactionRequestDTO(
                        categoryId,
                        new BigDecimal("5000.00"),
                        TransactionType.EXPENSE,
                        Frequency.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 12, 31)
                );

        RecurringTransaction recurringTransaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(user)
                        .category(category)
                        .amount(new BigDecimal("5000.00"))
                        .type(TransactionType.EXPENSE)
                        .frequency(Frequency.MONTHLY)
                        .startDate(LocalDate.of(2026, 8, 1))
                        .endDate(LocalDate.of(2026, 12, 31))
                        .active(true)
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(recurringTransactionRepository.save(any(
                RecurringTransaction.class)))
                .thenReturn(recurringTransaction);

        RecurringTransactionResponseDTO result =
                recurringTransactionService.createRecurringTransaction(
                        userId,
                        request
                );

        assertNotNull(result);

        assertEquals(
                1L,
                result.recurringTransactionId()
        );

        assertEquals(
                "Netflix",
                result.categoryName()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                result.amount()
        );

        assertEquals(
                TransactionType.EXPENSE,
                result.type()
        );

        assertEquals(
                Frequency.MONTHLY,
                result.frequency()
        );

        assertTrue(result.active());

        verify(recurringTransactionRepository)
                .save(any(RecurringTransaction.class));
    }


    // =========================================================
    // GET ACTIVE
    // =========================================================

    @Test
    void shouldGetActiveRecurringTransactions() {

        User user = new User();
        user.setUserId(1L);

        Category category = new Category();
        category.setName("Netflix");

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(user)
                        .category(category)
                        .amount(new BigDecimal("5000.00"))
                        .type(TransactionType.EXPENSE)
                        .frequency(Frequency.MONTHLY)
                        .startDate(LocalDate.of(2026, 8, 1))
                        .active(true)
                        .build();

        when(recurringTransactionRepository
                .findByUser_UserIdAndActiveTrue(1L))
                .thenReturn(List.of(transaction));

        List<RecurringTransactionResponseDTO> result =
                recurringTransactionService
                        .getAllRecurringTransactions(1L);

        assertEquals(1, result.size());

        assertTrue(result.get(0).active());

        assertEquals(
                "Netflix",
                result.get(0).categoryName()
        );

        verify(recurringTransactionRepository)
                .findByUser_UserIdAndActiveTrue(1L);
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void shouldGetAllRecurringTransactions() {

        User user = new User();
        user.setUserId(1L);

        Category category = new Category();
        category.setName("Internet");

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(user)
                        .category(category)
                        .amount(new BigDecimal("10000.00"))
                        .type(TransactionType.EXPENSE)
                        .frequency(Frequency.MONTHLY)
                        .startDate(LocalDate.of(2026, 8, 1))
                        .active(false)
                        .build();

        when(recurringTransactionRepository
                .findByUser_UserId(1L))
                .thenReturn(List.of(transaction));

        List<RecurringTransactionResponseDTO> result =
                recurringTransactionService
                        .getUserRecurringTransactions(1L);

        assertEquals(1, result.size());

        assertFalse(result.get(0).active());

        verify(recurringTransactionRepository)
                .findByUser_UserId(1L);
    }


    // =========================================================
    // PAUSE
    // =========================================================

 @Test
void shouldPauseRecurringTransaction() {

    Category category = new Category();
    category.setCategoryId(1L);
    category.setName("Food");

    RecurringTransaction transaction =
            RecurringTransaction.builder()
                    .id(1L)
                    .active(true)
                    .category(category)
                    .build();

    when(recurringTransactionRepository.findById(1L))
            .thenReturn(Optional.of(transaction));

    when(recurringTransactionRepository.save(transaction))
            .thenReturn(transaction);

    RecurringTransactionResponseDTO result =
            recurringTransactionService
                    .pauseRecurringTransaction(1L);

    assertFalse(transaction.isActive());

    verify(recurringTransactionRepository)
            .save(transaction);
}


    // =========================================================
    // PAUSE - NOT FOUND
    // =========================================================

    @Test
    void shouldThrowExceptionWhenPausingMissingTransaction() {

        when(recurringTransactionRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> recurringTransactionService
                        .pauseRecurringTransaction(99L)
        );

        assertEquals(
                "Recurring transaction not found",
                exception.getMessage()
        );

        verify(recurringTransactionRepository, never())
                .save(any());
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void shouldUpdateRecurringTransaction() {

        Long transactionId = 1L;
        Long userId = 1L;
        Long categoryId = 20L;

        User user = new User();
        user.setUserId(userId);

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Spotify");

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(transactionId)
                        .user(user)
                        .category(category)
                        .amount(new BigDecimal("3000.00"))
                        .type(TransactionType.EXPENSE)
                        .frequency(Frequency.MONTHLY)
                        .startDate(LocalDate.of(2026, 8, 1))
                        .active(true)
                        .build();

        RecurringTransactionRequestDTO request =
                new RecurringTransactionRequestDTO(
                        categoryId,
                        new BigDecimal("4000.00"),
                        TransactionType.EXPENSE,
                        Frequency.MONTHLY,
                        LocalDate.of(2026, 9, 1),
                        null
                );

        when(recurringTransactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(recurringTransactionRepository.save(transaction))
                .thenReturn(transaction);

        RecurringTransactionResponseDTO result =
                recurringTransactionService
                        .updateRecurringTransaction(
                                transactionId,
                                userId,
                                request
                        );

        assertEquals(
                new BigDecimal("4000.00"),
                transaction.getAmount()
        );

        assertEquals(
                LocalDate.of(2026, 9, 1),
                transaction.getStartDate()
        );

        assertEquals(
                new BigDecimal("4000.00"),
                result.amount()
        );

        verify(recurringTransactionRepository)
                .save(transaction);
    }


    // =========================================================
    // UPDATE - UNAUTHORIZED
    // =========================================================

    @Test
    void shouldRejectUnauthorizedUpdate() {

        User owner = new User();
        owner.setUserId(1L);

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(owner)
                        .build();

        when(recurringTransactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        RecurringTransactionRequestDTO request =
                new RecurringTransactionRequestDTO(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> recurringTransactionService
                        .updateRecurringTransaction(
                                1L,
                                2L,
                                request
                        )
        );

        assertEquals(
                "Unauthorized action",
                exception.getMessage()
        );

        verify(recurringTransactionRepository, never())
                .save(any());
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldDeleteRecurringTransaction() {

        User user = new User();
        user.setUserId(1L);

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(user)
                        .build();

        when(recurringTransactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        recurringTransactionService
                .deleteRecurringTransaction(1L, 1L);

        verify(recurringTransactionRepository)
                .delete(transaction);
    }


    // =========================================================
    // DELETE - UNAUTHORIZED
    // =========================================================

    @Test
    void shouldRejectUnauthorizedDelete() {

        User owner = new User();
        owner.setUserId(1L);

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(owner)
                        .build();

        when(recurringTransactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> recurringTransactionService
                        .deleteRecurringTransaction(1L, 2L)
        );

        assertEquals(
                "Unauthorized action",
                exception.getMessage()
        );

        verify(recurringTransactionRepository, never())
                .delete(any());
    }


    // =========================================================
    // PROCESS DAILY TRANSACTION
    // =========================================================

    @Test
    void shouldProcessDailyRecurringTransaction() {

        LocalDate today = LocalDate.now();

        User user = new User();
        user.setUserId(1L);

        Category category = new Category();
        category.setName("Daily Expense");

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(user)
                        .category(category)
                        .amount(new BigDecimal("1000.00"))
                        .type(TransactionType.EXPENSE)
                        .frequency(Frequency.DAILY)
                        .startDate(today.minusDays(5))
                        .endDate(today.plusDays(5))
                        .active(true)
                        .build();

        when(recurringTransactionRepository.findAll())
                .thenReturn(List.of(transaction));

        recurringTransactionService
                .processRecurringTransactions();

        verify(expenseRepository)
                .save(any(Expense.class));
    }


    // =========================================================
    // PROCESS INACTIVE TRANSACTION
    // =========================================================

    @Test
    void shouldNotProcessInactiveRecurringTransaction() {

        LocalDate today = LocalDate.now();

        User user = new User();
        user.setUserId(1L);

        Category category = new Category();
        category.setName("Inactive Expense");

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(user)
                        .category(category)
                        .amount(new BigDecimal("1000.00"))
                        .type(TransactionType.EXPENSE)
                        .frequency(Frequency.DAILY)
                        .startDate(today.minusDays(5))
                        .endDate(today.plusDays(5))
                        .active(false)
                        .build();

        when(recurringTransactionRepository.findAll())
                .thenReturn(List.of(transaction));

        recurringTransactionService
                .processRecurringTransactions();

        verify(expenseRepository, never())
                .save(any(Expense.class));
    }


    // =========================================================
    // PROCESS TRANSACTION BEFORE START DATE
    // =========================================================

    @Test
    void shouldNotProcessBeforeStartDate() {

        LocalDate today = LocalDate.now();

        User user = new User();
        user.setUserId(1L);

        Category category = new Category();
        category.setName("Future Expense");

        RecurringTransaction transaction =
                RecurringTransaction.builder()
                        .id(1L)
                        .user(user)
                        .category(category)
                        .amount(new BigDecimal("1000.00"))
                        .type(TransactionType.EXPENSE)
                        .frequency(Frequency.DAILY)
                        .startDate(today.plusDays(1))
                        .endDate(today.plusDays(10))
                        .active(true)
                        .build();

        when(recurringTransactionRepository.findAll())
                .thenReturn(List.of(transaction));

        recurringTransactionService
                .processRecurringTransactions();

        verify(expenseRepository, never())
                .save(any(Expense.class));
    }
}