package com.finance.tracker.service;

import com.finance.tracker.dto.DashboardResponseDTO;

public interface DashboardService {
    DashboardResponseDTO getDashboard(Long userId);
}
