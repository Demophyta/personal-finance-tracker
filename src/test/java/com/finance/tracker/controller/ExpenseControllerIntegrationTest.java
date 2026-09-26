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
class ExpenseControllerIntegrationTest {

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

    private static final String PASSWORD = "Password123!";


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Every test gets a completely new user.
         *
         * This prevents conflicts with users/categories/expenses
         * created by previous test runs.
         */

        String uniqueEmail =
                "expense." + System.nanoTime() + "@test.com";

        RegisterRequest registerRequest = new RegisterRequest();

        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setFirstName("Expense");
        registerRequest.setLastName("Integration");
        registerRequest.setPhoneNumber("08033333333");


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

        AuthRequest loginRequest = new AuthRequest();

        loginRequest.setEmail(uniqueEmail);
        loginRequest.setPassword(PASSWORD);

        String loginResponse = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
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

        User user = userRepository.findByEmail(uniqueEmail)
                .orElseThrow();

        userId = user.getUserId();


        // -----------------------------------------------------
        // CREATE TEST CATEGORY
        // -----------------------------------------------------

        Category category = Category.builder()
                .name("Expense Test Category " + System.nanoTime())
                .user(user)
                .build();

        categoryId =
                categoryRepository.save(category)
                        .getCategoryId();
    }


    // =========================================================
    // CREATE INCOME
    // =========================================================

    @Test
    void shouldCreateIncomeSuccessfully() throws Exception {

        String request = """
                {
                    "amount": 100000.00,
                    "date": "2026-08-20",
                    "description": "Integration test income",
                    "type": "INCOME",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expenseId").exists())
                .andExpect(jsonPath("$.amount")
                        .value(100000.00))
                .andExpect(jsonPath("$.type")
                        .value("INCOME"))
                .andExpect(jsonPath("$.description")
                        .value("Integration test income"));
    }


    // =========================================================
    // CREATE EXPENSE
    // =========================================================

    @Test
    void shouldCreateExpenseSuccessfully() throws Exception {

        // First create income.
        String incomeRequest = """
                {
                    "amount": 100000.00,
                    "date": "2026-08-20",
                    "description": "Starting income",
                    "type": "INCOME",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(incomeRequest)
                )
                .andExpect(status().isCreated());


        // Create expense.
        String expenseRequest = """
                {
                    "amount": 25000.00,
                    "date": "2026-08-20",
                    "description": "Integration test expense",
                    "type": "EXPENSE",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expenseId").exists())
                .andExpect(jsonPath("$.amount")
                        .value(25000.00))
                .andExpect(jsonPath("$.type")
                        .value("EXPENSE"))
                .andExpect(jsonPath("$.category")
                        .value(startsWith(
                                "Expense Test Category"
                        )));
    }


    // =========================================================
    // GET USER EXPENSES
    // =========================================================

    @Test
    void shouldGetUserExpensesSuccessfully() throws Exception {

        String incomeRequest = """
                {
                    "amount": 100000.00,
                    "date": "2026-08-20",
                    "description": "Income for expense test",
                    "type": "INCOME",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(incomeRequest)
                )
                .andExpect(status().isCreated());


        mockMvc.perform(
                        get("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].expenseId").exists());
    }


    // =========================================================
    // MONTHLY SUMMARY
    // =========================================================

    @Test
    void shouldGetMonthlySummarySuccessfully() throws Exception {

        // Create income.
        String incomeRequest = """
                {
                    "amount": 50000.00,
                    "date": "2026-08-10",
                    "description": "Monthly income",
                    "type": "INCOME",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(incomeRequest)
                )
                .andExpect(status().isCreated());


        // Create actual expense.
        String expenseRequest = """
                {
                    "amount": 15000.00,
                    "date": "2026-08-20",
                    "description": "Monthly expense",
                    "type": "EXPENSE",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseRequest)
                )
                .andExpect(status().isCreated());


        mockMvc.perform(
                        get("/api/expenses/" + userId + "/summary")
                                .param("year", "2026")
                                .param("month", "8")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year")
                        .value(2026))
                .andExpect(jsonPath("$.month")
                        .value(8))
                .andExpect(jsonPath("$.totalExpenses")
                        .value(15000.00))
                .andExpect(jsonPath("$.transactionCount")
                        .value(1));
    }


    // =========================================================
    // CATEGORY REPORT
    // =========================================================

    @Test
    void shouldGetCategoryReportSuccessfully() throws Exception {

        /*
         * IMPORTANT:
         *
         * This test is testing the CATEGORY REPORT endpoint.
         *
         * We therefore insert the expenses directly through the
         * repository instead of calling POST /api/expenses.
         *
         * That avoids unrelated balance/notification/budget
         * validation from affecting this report test.
         */


        User user = userRepository.findById(userId)
                .orElseThrow();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow();


        // -----------------------------------------------------
        // CREATE FIRST EXPENSE DIRECTLY
        // -----------------------------------------------------

        Expense expense1 = Expense.builder()
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2026, 8, 10))
                .description("Food expense one")
                .type(TransactionType.EXPENSE)
                .category(category)
                .user(user)
                .build();

        expenseRepository.save(expense1);


        // -----------------------------------------------------
        // CREATE SECOND EXPENSE DIRECTLY
        // -----------------------------------------------------

        Expense expense2 = Expense.builder()
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
                        get(
                                "/api/expenses/"
                                        + userId
                                        + "/category-report"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .param("year", "2026")
                                .param("month", "8")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())

                // Category name
                .andExpect(
                        jsonPath("$[0].categoryName")
                                .value(
                                        startsWith(
                                                "Expense Test Category"
                                        )
                                )
                )

                // DTO field is totalAmount
                .andExpect(
                        jsonPath("$[0].totalAmount")
                                .value(8000.00)
                )

                // DTO field is transactionCount
                .andExpect(
                        jsonPath("$[0].transactionCount")
                                .value(2)
                );
    }


    // =========================================================
    // YEARLY REPORT
    // =========================================================

    @Test
    void shouldGetYearlyReportSuccessfully() throws Exception {

        // Create income.
        String incomeRequest = """
                {
                    "amount": 50000.00,
                    "date": "2026-08-01",
                    "description": "Yearly report income",
                    "type": "INCOME",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(incomeRequest)
                )
                .andExpect(status().isCreated());


        // Create expense.
        String expenseRequest = """
                {
                    "amount": 12000.00,
                    "date": "2026-08-20",
                    "description": "Yearly report expense",
                    "type": "EXPENSE",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseRequest)
                )
                .andExpect(status().isCreated());


        // Get yearly report.
        mockMvc.perform(
                        get(
                                "/api/expenses/"
                                        + userId
                                        + "/yearly-report"
                        )
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year")
                        .value(2026))
                .andExpect(jsonPath("$.totalExpenses")
                        .value(12000.00));
    }


    // =========================================================
    // INVALID AMOUNT
    // =========================================================

    @Test
    void shouldRejectInvalidAmount() throws Exception {

        String request = """
                {
                    "amount": 0,
                    "date": "2026-08-20",
                    "description": "Invalid amount",
                    "type": "INCOME",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"));
    }


    // =========================================================
    // UNAUTHENTICATED CREATE
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedCreateExpense() throws Exception {

        String request = """
                {
                    "amount": 10000.00,
                    "date": "2026-08-20",
                    "description": "Unauthorized",
                    "type": "INCOME",
                    "categoryId": %d
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED GET
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedGetExpenses() throws Exception {

        mockMvc.perform(
                        get("/api/expenses/" + userId)
                )
                .andExpect(status().isUnauthorized());
    }
}