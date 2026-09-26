package com.finance.tracker.service;

import com.finance.tracker.dto.TransactionHistoryDTO;
import java.time.LocalDate;
import java.util.List;

public interface TransactionHistoryService {
    List<TransactionHistoryDTO> getUserTransactions(Long userId, LocalDate start, LocalDate end);
}
