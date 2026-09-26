package com.finance.tracker.controller;

import com.finance.tracker.dto.RecurringTransactionRequestDTO;
import com.finance.tracker.dto.RecurringTransactionResponseDTO;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;
    private final UserRepository userRepository;

    // ✅ Create recurring transaction
    @PostMapping
    public ResponseEntity<RecurringTransactionResponseDTO> createRecurringTransaction(
            @RequestBody RecurringTransactionRequestDTO requestDTO,
            Principal principal
    ) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.status(201)
                .body(recurringTransactionService.createRecurringTransaction(user.getUserId(), requestDTO));
    }

    // ✅ Get all recurring transactions for logged-in user
    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponseDTO>> getRecurringTransactions(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(recurringTransactionService.getUserRecurringTransactions(user.getUserId()));
    }

    // ✅ Update recurring transaction
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponseDTO> updateRecurringTransaction(
            @PathVariable Long id,
            @RequestBody RecurringTransactionRequestDTO requestDTO,
            Principal principal
    ) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                recurringTransactionService.updateRecurringTransaction(id, user.getUserId(), requestDTO)
        );
    }

    // ✅ Delete recurring transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecurringTransaction(
            @PathVariable Long id,
            Principal principal
    ) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        recurringTransactionService.deleteRecurringTransaction(id, user.getUserId());
        return ResponseEntity.ok("Recurring transaction deleted successfully");
    }
}
