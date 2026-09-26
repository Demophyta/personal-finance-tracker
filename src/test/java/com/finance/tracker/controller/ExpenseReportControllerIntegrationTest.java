package com.finance.tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.RegisterRequest;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.Expense;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ExpenseReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private String jwtToken;

    private Long userId;

    private Long categoryId;

    private static final String PASSWORD =
            "Password123!";


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Each test gets a unique user.
         *
         * We don't clear the database because of the foreign-key
         * relationships between users, categories and expenses.
         */

        String uniqueEmail =
                "expense.report."
                        + System.nanoTime()
                        + "@test.com";


        RegisterRequest registerRequest =
                new RegisterRequest();

        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setFirstName("Expense");
        registerRequest.setLastName("Report");
        registerRequest.setPhoneNumber("08066666666");


        // -----------------------------------------------------
        // REGISTER
        // -----------------------------------------------------

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                registerRequest
                                        )
                                )
                )
                .andExpect(status().isOk());


        // -----------------------------------------------------
        // LOGIN
        // -----------------------------------------------------

        AuthRequest loginRequest =
                new AuthRequest();

        loginRequest.setEmail(uniqueEmail);
        loginRequest.setPassword(PASSWORD);


        String loginResponse =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        loginRequest
                                                )
                                        )
                        )
                        .andExpect(status().isOk())
                        .andExpect(
                                jsonPath("$.token").exists()
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();


        JsonNode loginJson =
                objectMapper.readTree(loginResponse);

        jwtToken =
                loginJson.get("token").asText();


        // -----------------------------------------------------
        // GET USER
        // -----------------------------------------------------

        User user =
                userRepository.findByEmail(uniqueEmail)
                        .orElseThrow();

        userId =
                user.getUserId();


        // -----------------------------------------------------
        // CREATE CATEGORY
        // -----------------------------------------------------

        Category category =
                Category.builder()
                        .name(
                                "Report Test Category "
                                        + System.nanoTime()
                        )
                        .user(user)
                        .build();

        categoryId =
                categoryRepository.save(category)
                        .getCategoryId();
    }


    // =========================================================
    // MONTHLY SUMMARY
    // =========================================================

    @Test
    void shouldGetMonthlySummarySuccessfully()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow();


        /*
         * Insert test data directly.
         *
         * This test is specifically testing the report endpoint,
         * so we don't need the expense creation endpoint involved.
         */

        Expense expense1 =
                Expense.builder()
                        .amount(new BigDecimal("5000.00"))
                        .date(LocalDate.of(2026, 8, 10))
                        .description("August expense one")
                        .type(TransactionType.EXPENSE)
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(expense1);


        Expense expense2 =
                Expense.builder()
                        .amount(new BigDecimal("3000.00"))
                        .date(LocalDate.of(2026, 8, 15))
                        .description("August expense two")
                        .type(TransactionType.EXPENSE)
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(expense2);


        /*
         * Income should not be counted as an expense.
         */

        Expense income =
                Expense.builder()
                        .amount(new BigDecimal("50000.00"))
                        .date(LocalDate.of(2026, 8, 5))
                        .description("August income")
                        .type(TransactionType.INCOME)
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(income);


        // -----------------------------------------------------
        // GET MONTHLY REPORT
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/reports/expenses/summary")
                                .param("year", "2026")
                                .param("month", "8")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.year")
                                .value(2026)
                )
                .andExpect(
                        jsonPath("$.month")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.totalExpenses")
                                .value(8000.00)
                )
                .andExpect(
                        jsonPath("$.transactionCount")
                                .value(2)
                );
    }


    // =========================================================
    // CATEGORY REPORT
    // =========================================================

    @Test
    void shouldGetCategoryReportSuccessfully()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow();


        // -----------------------------------------------------
        // FIRST EXPENSE
        // -----------------------------------------------------

        Expense expense1 =
                Expense.builder()
                        .amount(new BigDecimal("5000.00"))
                        .date(LocalDate.of(2026, 8, 10))
                        .description("Food expense one")
                        .type(TransactionType.EXPENSE)
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(expense1);


        // -----------------------------------------------------
        // SECOND EXPENSE
        // -----------------------------------------------------

        Expense expense2 =
                Expense.builder()
                        .amount(new BigDecimal("3000.00"))
                        .date(LocalDate.of(2026, 8, 15))
                        .description("Food expense two")
                        .type(TransactionType.EXPENSE)
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(expense2);


        // -----------------------------------------------------
        // GET CATEGORY REPORT
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/reports/expenses/by-category")
                                .param("year", "2026")
                                .param("month", "8")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(
                        jsonPath("$[0].categoryName")
                                .value(
                                        startsWith(
                                                "Report Test Category"
                                        )
                                )
                )
                .andExpect(
                        jsonPath("$[0].totalAmount")
                                .value(8000.00)
                )
                .andExpect(
                        jsonPath("$[0].transactionCount")
                                .value(2)
                );
    }


    // =========================================================
    // YEARLY REPORT
    // =========================================================

    @Test
    void shouldGetYearlyReportSuccessfully()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow();


        // -----------------------------------------------------
        // AUGUST EXPENSE
        // -----------------------------------------------------

        Expense augustExpense =
                Expense.builder()
                        .amount(new BigDecimal("12000.00"))
                        .date(LocalDate.of(2026, 8, 20))
                        .description("August yearly expense")
                        .type(TransactionType.EXPENSE)
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(augustExpense);


        // -----------------------------------------------------
        // FEBRUARY EXPENSE
        // -----------------------------------------------------

        Expense februaryExpense =
                Expense.builder()
                        .amount(new BigDecimal("8000.00"))
                        .date(LocalDate.of(2026, 2, 10))
                        .description("February yearly expense")
                        .type(TransactionType.EXPENSE)
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(februaryExpense);


        // -----------------------------------------------------
        // DIFFERENT YEAR — MUST NOT BE INCLUDED
        // -----------------------------------------------------

        Expense previousYearExpense =
                Expense.builder()
                        .amount(new BigDecimal("50000.00"))
                        .date(LocalDate.of(2025, 8, 20))
                        .description("Previous year expense")
                        .type(TransactionType.EXPENSE)
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(previousYearExpense);


        // -----------------------------------------------------
        // GET YEARLY REPORT
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/reports/expenses/yearly")
                                .param("year", "2026")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.year")
                                .value(2026)
                )
                .andExpect(
                        jsonPath("$.totalExpenses")
                                .value(20000.00)
                );
    }


    // =========================================================
    // MONTH WITH NO EXPENSES
    // =========================================================

    @Test
    void shouldReturnEmptyMonthlySummaryWhenNoExpensesExist()
            throws Exception {

        mockMvc.perform(
                        get("/api/reports/expenses/summary")
                                .param("year", "2026")
                                .param("month", "1")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.year")
                                .value(2026)
                )
                .andExpect(
                        jsonPath("$.month")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.totalExpenses")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.transactionCount")
                                .value(0)
                );
    }


    // =========================================================
    // CATEGORY REPORT WITH NO EXPENSES
    // =========================================================

    @Test
    void shouldReturnEmptyCategoryReportWhenNoExpensesExist()
            throws Exception {

        mockMvc.perform(
                        get("/api/reports/expenses/by-category")
                                .param("year", "2026")
                                .param("month", "1")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }


    // =========================================================
    // YEARLY REPORT WITH NO EXPENSES
    // =========================================================

    @Test
    void shouldReturnEmptyYearlyReportWhenNoExpensesExist()
            throws Exception {

        mockMvc.perform(
                        get("/api/reports/expenses/yearly")
                                .param("year", "2024")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.year")
                                .value(2024)
                )
                .andExpect(
                        jsonPath("$.totalExpenses")
                                .value(0)
                );
    }


    // =========================================================
    // UNAUTHENTICATED MONTHLY REPORT
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedMonthlyReport()
            throws Exception {

        mockMvc.perform(
                        get("/api/reports/expenses/summary")
                                .param("year", "2026")
                                .param("month", "8")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED CATEGORY REPORT
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedCategoryReport()
            throws Exception {

        mockMvc.perform(
                        get("/api/reports/expenses/by-category")
                                .param("year", "2026")
                                .param("month", "8")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED YEARLY REPORT
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedYearlyReport()
            throws Exception {

        mockMvc.perform(
                        get("/api/reports/expenses/yearly")
                                .param("year", "2026")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isUnauthorized());
    }
}