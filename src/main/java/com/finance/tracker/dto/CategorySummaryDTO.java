package com.finance.tracker.dto;

import java.math.BigDecimal;

public record CategorySummaryDTO(
        String categoryName,
        BigDecimal totalSpent
) {}