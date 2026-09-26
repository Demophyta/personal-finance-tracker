package com.finance.tracker.controller;

import com.finance.tracker.dto.BudgetRequestDTO;
import com.finance.tracker.dto.BudgetResponseDTO;
import com.finance.tracker.dto.BudgetUsageDTO;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> createBudget(
            @RequestBody BudgetRequestDTO requestDTO,
            Principal principal) {

        User user = getAuthenticatedUser(principal);

        BudgetResponseDTO response =
                budgetService.createBudget(user.getUserId(), requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @PathVariable Long id,
            @RequestBody BudgetRequestDTO requestDTO,
            Principal principal) {

        User user = getAuthenticatedUser(principal);

        BudgetResponseDTO response =
                budgetService.updateBudget(id, user.getUserId(), requestDTO);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBudget(
            @PathVariable Long id,
            Principal principal) {

        User user = getAuthenticatedUser(principal);

        budgetService.deleteBudget(id, user.getUserId());

        return ResponseEntity.ok("Budget deleted successfully.");
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponseDTO>> getUserBudgets(
            Principal principal) {

        User user = getAuthenticatedUser(principal);

        return ResponseEntity.ok(
                budgetService.getUserBudgets(user.getUserId())
        );
    }

    @GetMapping("/usage")
    public ResponseEntity<List<BudgetUsageDTO>> getBudgetUsage(
            Principal principal) {

        User user = getAuthenticatedUser(principal);

        return ResponseEntity.ok(
                budgetService.getBudgetUsage(user.getUserId())
        );
    }

    /**
     * Returns the currently authenticated user.
     */
    private User getAuthenticatedUser(Principal principal) {

        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}