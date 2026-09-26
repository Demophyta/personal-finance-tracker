package com.finance.tracker.dto;

public record BudgetStatus(
        BudgetUsageDTO usage,

        boolean warning,

        boolean exceeded

) {
}
