package com.finance.tracker.repository;

import com.finance.tracker.model.Budget;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Returns all budgets belonging to a user.
     */
    List<Budget> findByUser_UserId(Long userId);

    /**
     * Returns all active budgets for a user on a specific date.
     */
    @Query("""
            SELECT b
            FROM Budget b
            WHERE b.user = :user
              AND :date BETWEEN b.startDate AND b.endDate
            """)
    List<Budget> findActiveBudgets(
            @Param("user") User user,
            @Param("date") LocalDate date
    );

    /**
     * Finds the active budget for a category on a specific date.
     */
    @Query("""
            SELECT b
            FROM Budget b
            WHERE b.user = :user
              AND b.category = :category
              AND :date BETWEEN b.startDate AND b.endDate
            """)
    Optional<Budget> findActiveBudget(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("date") LocalDate date
    );
}