package com.finance.tracker.service;

import com.finance.tracker.dto.RecurringTransactionRequestDTO;
import com.finance.tracker.dto.RecurringTransactionResponseDTO;
import com.finance.tracker.model.*;
import com.finance.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // CREATE
    @Override
    public RecurringTransactionResponseDTO createRecurringTransaction(Long userId, RecurringTransactionRequestDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        RecurringTransaction rt = RecurringTransaction.builder()
                .user(user)
                .category(category)
                .amount(request.amount())
                .type(request.type())           // ENUM OK
                .frequency(request.frequency()) // ENUM OK
                .startDate(request.startDate())
                .endDate(request.endDate())
                .active(true)
                .build();

        recurringTransactionRepository.save(rt);
        return mapToResponse(rt);
    }

    // GET ACTIVE
    @Override
    public List<RecurringTransactionResponseDTO> getAllRecurringTransactions(Long userId) {
        return recurringTransactionRepository.findByUser_UserIdAndActiveTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET ALL
    @Override
    public List<RecurringTransactionResponseDTO> getUserRecurringTransactions(Long userId) {
        return recurringTransactionRepository.findByUser_UserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // PAUSE
    @Override
    public RecurringTransactionResponseDTO pauseRecurringTransaction(Long id) {

        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));

        rt.setActive(false);
        recurringTransactionRepository.save(rt);

        return mapToResponse(rt);
    }

    // UPDATE
    @Override
    public RecurringTransactionResponseDTO updateRecurringTransaction(Long id, Long userId, RecurringTransactionRequestDTO request) {

        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));

        if (!rt.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized action");
        }

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            rt.setCategory(category);
        }

        if (request.amount() != null) rt.setAmount(request.amount());
        if (request.type() != null) rt.setType(request.type());                 // ENUM
        if (request.frequency() != null) rt.setFrequency(request.frequency());  // ENUM
        if (request.startDate() != null) rt.setStartDate(request.startDate());
        if (request.endDate() != null) rt.setEndDate(request.endDate());

        recurringTransactionRepository.save(rt);
        return mapToResponse(rt);
    }

    // DELETE
    @Override
    public void deleteRecurringTransaction(Long id, Long userId) {

        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));

        if (!rt.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized action");
        }

        recurringTransactionRepository.delete(rt);
    }

    // SCHEDULER
    @Override
    @Scheduled(cron = "0 0 1 * * *")
    public void processRecurringTransactions() {

        LocalDate today = LocalDate.now();

        List<RecurringTransaction> list = recurringTransactionRepository.findAll();

        for (RecurringTransaction rt : list) {

            if (rt.isActive() && shouldTriggerToday(rt, today)) {

                Expense expense = Expense.builder()
                        .user(rt.getUser())
                        .category(rt.getCategory())
                        .amount(rt.getAmount())
                        .date(today)
                        .type(rt.getType()) // ENUM OK
                        .description("Recurring: " + rt.getCategory().getName())
                        .build();

                expenseRepository.save(expense);
            }
        }
    }

    // FIXED: ENUM VERSION (NO toUpperCase)
    private boolean shouldTriggerToday(RecurringTransaction rt, LocalDate today) {

        if (today.isBefore(rt.getStartDate()) ||
                (rt.getEndDate() != null && today.isAfter(rt.getEndDate()))) {
            return false;
        }

        return switch (rt.getFrequency()) {

            case DAILY -> true;

            case WEEKLY ->
                    today.getDayOfWeek().equals(rt.getStartDate().getDayOfWeek());

            case MONTHLY ->
                    today.getDayOfMonth() == rt.getStartDate().getDayOfMonth();
        };
    }

    // DTO MAPPER
    private RecurringTransactionResponseDTO mapToResponse(RecurringTransaction rt) {

        return new RecurringTransactionResponseDTO(
                rt.getId(),
                rt.getCategory().getName(),
                rt.getAmount(),
                rt.getType(),
                rt.getFrequency(),
                rt.getStartDate(),
                rt.getEndDate(),
                rt.isActive()
        );
    }
}