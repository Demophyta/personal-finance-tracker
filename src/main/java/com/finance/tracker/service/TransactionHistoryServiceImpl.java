package com.finance.tracker.service;

import com.finance.tracker.dto.TransactionHistoryDTO;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.model.Expense;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Override
    public List<TransactionHistoryDTO> getUserTransactions(
            Long userId,
            LocalDate start,
            LocalDate end) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return expenseRepository
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        start,
                        end
                )
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private TransactionHistoryDTO mapToDTO(Expense expense) {

        return new TransactionHistoryDTO(
                expense.getExpenseId(),
                expense.getCategory() != null
                        ? expense.getCategory().getName()
                        : "Income",
                expense.getAmount(),
                expense.getType().name(),
                expense.getDescription(),
                expense.getDate()
        );
    }
}