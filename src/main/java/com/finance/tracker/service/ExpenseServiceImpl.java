package com.finance.tracker.service;

import com.finance.tracker.dto.*;
import com.finance.tracker.exception.CategoryNotFoundException;
import com.finance.tracker.exception.InsufficientBalanceException;
import com.finance.tracker.exception.InvalidAmountException;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.model.*;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCalculationService budgetCalculationService;
    private final NotificationService notificationService;
    private final BalanceService balanceService;

    @Override
    public ExpenseResponseDTO createExpense(
            ExpenseRequestDTO request,
            Long userId) {

        User user = validateUser(userId);

        Category category = validateCategory(request.categoryId());

        validateAmount(request.amount());

        validateBalance(user, request);

        Expense savedExpense = saveExpense(user, category, request);

        createExpenseNotification(savedExpense);

        checkBudget(user, category, savedExpense);

        return mapToResponse(savedExpense);
    }

    @Override
    public List<ExpenseResponseDTO> getUserExpenses(Long userId) {

        User user = validateUser(userId);

        return expenseRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ExpenseSummaryDTO getMonthlySummary(Long userId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Expense> expenses =
                expenseRepository.findByUser_UserIdAndTypeAndDateBetween(
                        userId,
                        TransactionType.EXPENSE,
                        start,
                        end
                );

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExpenseSummaryDTO(
                year,
                month,
                total,
                expenses.size()
        );
    }

    @Override
    public List<CategoryExpenseDTO> getCategoryReport(
            Long userId,
            int year,
            int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Expense> expenses =
                expenseRepository.findByUser_UserIdAndTypeAndDateBetween(
                        userId,
                        TransactionType.EXPENSE,
                        start,
                        end
                );

        return expenses.stream()

                .filter(e -> e.getCategory() != null)

                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),

                        Collectors.collectingAndThen(

                                Collectors.toList(),

                                list -> new CategoryExpenseDTO(

                                        list.get(0).getCategory().getName(),

                                        list.stream()
                                                .map(Expense::getAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add),

                                        list.size()
                                )
                        )
                ))
                .values()
                .stream()
                .toList();
    }

    @Override
    public YearlyExpenseDTO getYearlyReport(Long userId, int year) {

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<Expense> expenses =
                expenseRepository.findByUser_UserIdAndTypeAndDateBetween(
                        userId,
                        TransactionType.EXPENSE,
                        start,
                        end
                );

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new YearlyExpenseDTO(year, total);
    }


    // Private Helper Methods

    private User validateUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private Category validateCategory(Long categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException();

        }
    }

    private void validateBalance(
            User user,
            ExpenseRequestDTO request) {

        if (request.type() != TransactionType.EXPENSE) {
            return;
        }

        BigDecimal balance =
                balanceService.getCurrentBalance(user.getUserId());

        if (request.amount().compareTo(balance) > 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance. Current balance is ₦" + balance
            );
        }
    }

    private Expense saveExpense(
            User user,
            Category category,
            ExpenseRequestDTO request) {

        Expense expense = Expense.builder()
                .amount(request.amount())
                .date(request.date() != null ? request.date() : LocalDate.now())
                .description(request.description())
                .type(request.type())
                .category(category)
                .user(user)
                .build();

        return expenseRepository.save(expense);
    }

    private void createExpenseNotification(Expense expense) {

        if (expense.getType() == TransactionType.INCOME) {

            notificationService.createNotification(
                    expense.getUser(),
                    "Income of ₦" + expense.getAmount() + " recorded."
            );

        } else {

            notificationService.createNotification(
                    expense.getUser(),
                    "Expense of ₦" + expense.getAmount()
                            + " recorded under "
                            + expense.getCategory().getName() + "."
            );
        }
    }

    private void checkBudget(
            User user,
            Category category,
            Expense expense) {

        if (expense.getType() != TransactionType.EXPENSE) {
            return;
        }

        budgetRepository.findActiveBudget(
                user,
                category,
                expense.getDate()
        ).ifPresent(budget -> {

            BudgetStatus status =
                    budgetCalculationService.calculateStatus(budget);

            if (status.warning()) {

                notificationService.sendBudgetAlert(
                        user,
                        String.format(
                                "%s budget is %.2f%% used.",
                                category.getName(),
                                status.usage().getUsagePercentage()
                        )
                );
            }

            if (status.exceeded()) {

                notificationService.sendBudgetAlert(
                        user,
                        String.format(
                                "%s budget exceeded by ₦%s.",
                                category.getName(),
                                status.usage().getRemainingAmount().abs()
                        )
                );
            }
        });
    }

    private ExpenseResponseDTO mapToResponse(Expense expense) {

        return new ExpenseResponseDTO(
                expense.getExpenseId(),
                expense.getAmount(),
                expense.getDate(),
                expense.getDescription(),
                expense.getType(),
                expense.getCategory() != null
                        ? expense.getCategory().getName()
                        : null,
                expense.getCreatedAt()
        );
    }
}