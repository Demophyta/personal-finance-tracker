package com.finance.tracker.controller;

import com.finance.tracker.dto.CategoryExpenseDTO;
import com.finance.tracker.dto.ExpenseRequestDTO;
import com.finance.tracker.dto.ExpenseResponseDTO;
import com.finance.tracker.dto.ExpenseSummaryDTO;
import com.finance.tracker.dto.YearlyExpenseDTO;
import com.finance.tracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/{userId}")
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @PathVariable Long userId,
            @RequestBody ExpenseRequestDTO request) {

        ExpenseResponseDTO response =
                expenseService.createExpense(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ExpenseResponseDTO>> getUserExpenses(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                expenseService.getUserExpenses(userId)
        );
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<ExpenseSummaryDTO> getMonthlySummary(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {

        return ResponseEntity.ok(
                expenseService.getMonthlySummary(userId, year, month)
        );
    }

    @GetMapping("/{userId}/category-report")
    public ResponseEntity<List<CategoryExpenseDTO>> getCategoryReport(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {

        return ResponseEntity.ok(
                expenseService.getCategoryReport(userId, year, month)
        );
    }

    @GetMapping("/{userId}/yearly-report")
    public ResponseEntity<YearlyExpenseDTO> getYearlyReport(
            @PathVariable Long userId,
            @RequestParam int year) {

        return ResponseEntity.ok(
                expenseService.getYearlyReport(userId, year)
        );
    }
}