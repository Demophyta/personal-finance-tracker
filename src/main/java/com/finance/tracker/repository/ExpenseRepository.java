package com.finance.tracker.repository;

import com.finance.tracker.model.Expense;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // =====================================================
    // Basic Queries
    // =====================================================

    List<Expense> findByUser(User user);

    List<Expense> findByUser(User user, Sort sort);

    List<Expense> findByUser_UserId(Long userId);
    List<Expense> findByUserAndDateBetweenOrderByDateDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    // =====================================================
    // Transaction Type Queries
    // =====================================================

    List<Expense> findByUserAndType(
            User user,
            TransactionType type
    );

    List<Expense> findByUser_UserIdAndType(
            Long userId,
            TransactionType type
    );

    List<Expense> findByUser_UserIdAndTypeAndDateBetween(
            Long userId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Expense> findByUserAndTypeAndDateBetween(
            User user,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );

    // =====================================================
    // Search
    // =====================================================

    List<Expense> findByUserAndCategory_NameContainingIgnoreCase(
            User user,
            String categoryName,
            Sort sort
    );

    // =====================================================
    // Budget
    // =====================================================

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user.userId = :userId
          AND e.category.categoryId = :categoryId
          AND e.type = com.finance.tracker.model.TransactionType.EXPENSE
          AND e.date BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumByUserIdAndCategoryAndDateBetween(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // =====================================================
    // Balance
    // =====================================================

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user.userId = :userId
          AND e.type = com.finance.tracker.model.TransactionType.INCOME
    """)
    BigDecimal getTotalIncome(@Param("userId") Long userId);

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user.userId = :userId
          AND e.type = com.finance.tracker.model.TransactionType.EXPENSE
    """)
    BigDecimal getTotalExpenses(@Param("userId") Long userId);


    /**
     * Calculates total income for a month.
     */
    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.user.userId = :userId
      AND e.type = com.finance.tracker.model.TransactionType.INCOME
      AND e.date BETWEEN :startDate AND :endDate
""")
    BigDecimal getMonthlyIncome(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculates total expenses for a month.
     */
    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.user.userId = :userId
      AND e.type = com.finance.tracker.model.TransactionType.EXPENSE
      AND e.date BETWEEN :startDate AND :endDate
""")
    BigDecimal getMonthlyExpenses(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}