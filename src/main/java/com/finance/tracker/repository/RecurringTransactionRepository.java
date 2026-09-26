package com.finance.tracker.repository;

import com.finance.tracker.model.RecurringTransaction;
import com.finance.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    // ✅ Find all active recurring transactions for a specific user
    List<RecurringTransaction> findByUser_UserIdAndActiveTrue(Long userId);

    // ✅ Find all recurring transactions (active + inactive) for a specific user
    List<RecurringTransaction> findByUser_UserId(Long userId);

    // ✅ Optional: find all recurring transactions by User entity (less common but useful)
    List<RecurringTransaction> findByUser(User user);
}