package com.finance.tracker.service;

import com.finance.tracker.dto.RecurringTransactionRequestDTO;
import com.finance.tracker.dto.RecurringTransactionResponseDTO;
import java.util.List;

public interface RecurringTransactionService {

    RecurringTransactionResponseDTO createRecurringTransaction(Long userId, RecurringTransactionRequestDTO request);

    List<RecurringTransactionResponseDTO> getAllRecurringTransactions(Long userId);

    List<RecurringTransactionResponseDTO> getUserRecurringTransactions(Long userId);

    RecurringTransactionResponseDTO pauseRecurringTransaction(Long id);

    // 👇 Updated: include userId
    RecurringTransactionResponseDTO updateRecurringTransaction(Long id, Long userId, RecurringTransactionRequestDTO request);

    // 👇 Updated: include userId
    void deleteRecurringTransaction(Long id, Long userId);

    void processRecurringTransactions();
}

