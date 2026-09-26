package com.finance.tracker.controller;

import com.finance.tracker.dto.CategoryExpenseDTO;
import com.finance.tracker.dto.ExpenseSummaryDTO;
import com.finance.tracker.dto.YearlyExpenseDTO;
import com.finance.tracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/expenses")
@RequiredArgsConstructor
public class ExpenseReportController {

    private final ExpenseService reportService;

    // Monthly summary
    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryDTO> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getMonthlySummary(userId, year, month));
    }

    // By category
    @GetMapping("/by-category")
    public ResponseEntity<List<CategoryExpenseDTO>> getCategoryReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getCategoryReport(userId, year, month));
    }

    // Yearly
    @GetMapping("/yearly")
    public ResponseEntity<YearlyExpenseDTO> getYearlyReport(
            @RequestParam int year,
            @RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getYearlyReport(userId, year));
    }
}
