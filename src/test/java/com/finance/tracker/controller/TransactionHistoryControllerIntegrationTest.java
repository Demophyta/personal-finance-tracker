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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TransactionHistoryControllerIntegrationTest {

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
         * Every test receives a unique user.
         *
         * This prevents test data from previous tests from
         * affecting the current test.
         */

        String uniqueEmail =
                "transaction." + System.nanoTime() + "@test.com";

        RegisterRequest registerRequest =
                new RegisterRequest();

        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setFirstName("Transaction");
        registerRequest.setLastName("Integration");
        registerRequest.setPhoneNumber("08044444444");


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

        userId = user.getUserId();


        // -----------------------------------------------------
        // CREATE TEST CATEGORY
        // -----------------------------------------------------

        Category category =
                Category.builder()
                        .name(
                                "Transaction Test Category "
                                        + System.nanoTime()
                        )
                        .user(user)
                        .build();

        categoryId =
                categoryRepository.save(category)
                        .getCategoryId();
    }


    // =========================================================
    // GET TRANSACTIONS SUCCESSFULLY
    // =========================================================

    @Test
    void shouldGetUserTransactionsSuccessfully()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow();


        // -----------------------------------------------------
        // CREATE OLDER TRANSACTION
        // -----------------------------------------------------

        Expense olderExpense =
                Expense.builder()
                        .amount(
                                new BigDecimal("5000.00")
                        )
                        .date(
                                LocalDate.of(
                                        2026,
                                        8,
                                        5
                                )
                        )
                        .description(
                                "Older transaction"
                        )
                        .type(
                                TransactionType.EXPENSE
                        )
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(olderExpense);


        // -----------------------------------------------------
        // CREATE NEWER TRANSACTION
        // -----------------------------------------------------

        Expense newerExpense =
                Expense.builder()
                        .amount(
                                new BigDecimal("15000.00")
                        )
                        .date(
                                LocalDate.of(
                                        2026,
                                        8,
                                        20
                                )
                        )
                        .description(
                                "Newer transaction"
                        )
                        .type(
                                TransactionType.EXPENSE
                        )
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(newerExpense);


        // -----------------------------------------------------
        // GET TRANSACTIONS
        // -----------------------------------------------------

        mockMvc.perform(
                        get("/api/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .param(
                                        "start",
                                        "2026-08-01"
                                )
                                .param(
                                        "end",
                                        "2026-08-31"
                                )
                )
                .andExpect(status().isOk())

                // Response must be an array
                .andExpect(
                        jsonPath("$").isArray()
                )

                // We created exactly two transactions
                .andExpect(
                        jsonPath("$", hasSize(2))
                )

                // Newest transaction should be first
                .andExpect(
                        jsonPath("$[0].amount")
                                .value(15000.00)
                )
                .andExpect(
                        jsonPath("$[0].description")
                                .value(
                                        "Newer transaction"
                                )
                )
                .andExpect(
                        jsonPath("$[0].date")
                                .value("2026-08-20")
                )

                // Older transaction should be second
                .andExpect(
                        jsonPath("$[1].amount")
                                .value(5000.00)
                )
                .andExpect(
                        jsonPath("$[1].description")
                                .value(
                                        "Older transaction"
                                )
                )
                .andExpect(
                        jsonPath("$[1].date")
                                .value("2026-08-05")
                );
    }


    // =========================================================
    // VERIFY TRANSACTION DTO FIELDS
    // =========================================================

    @Test
    void shouldReturnCorrectTransactionDetails()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow();


        Expense expense =
                Expense.builder()
                        .amount(
                                new BigDecimal("12000.00")
                        )
                        .date(
                                LocalDate.of(
                                        2026,
                                        8,
                                        15
                                )
                        )
                        .description(
                                "Transaction details test"
                        )
                        .type(
                                TransactionType.EXPENSE
                        )
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(expense);


        mockMvc.perform(
                        get("/api/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .param(
                                        "start",
                                        "2026-08-01"
                                )
                                .param(
                                        "end",
                                        "2026-08-31"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$", hasSize(1))
                )

                // ID
                .andExpect(
                        jsonPath("$[0].id").exists()
                )

                // Category
                .andExpect(
                        jsonPath("$[0].categoryName")
                                .value(
                                        startsWith(
                                                "Transaction Test Category"
                                        )
                                )
                )

                // Amount
                .andExpect(
                        jsonPath("$[0].amount")
                                .value(12000.00)
                )

                // Type
                .andExpect(
                        jsonPath("$[0].type")
                                .value("EXPENSE")
                )

                // Description
                .andExpect(
                        jsonPath("$[0].description")
                                .value(
                                        "Transaction details test"
                                )
                )

                // Date
                .andExpect(
                        jsonPath("$[0].date")
                                .value("2026-08-15")
                );
    }


    // =========================================================
    // FILTER BY DATE RANGE
    // =========================================================

    @Test
    void shouldReturnOnlyTransactionsWithinDateRange()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow();


        // -----------------------------------------------------
        // OUTSIDE RANGE - JULY
        // -----------------------------------------------------

        Expense julyExpense =
                Expense.builder()
                        .amount(
                                new BigDecimal("7000.00")
                        )
                        .date(
                                LocalDate.of(
                                        2026,
                                        7,
                                        25
                                )
                        )
                        .description(
                                "July transaction"
                        )
                        .type(
                                TransactionType.EXPENSE
                        )
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(julyExpense);


        // -----------------------------------------------------
        // INSIDE RANGE - AUGUST
        // -----------------------------------------------------

        Expense augustExpense =
                Expense.builder()
                        .amount(
                                new BigDecimal("9000.00")
                        )
                        .date(
                                LocalDate.of(
                                        2026,
                                        8,
                                        15
                                )
                        )
                        .description(
                                "August transaction"
                        )
                        .type(
                                TransactionType.EXPENSE
                        )
                        .category(category)
                        .user(user)
                        .build();

        expenseRepository.save(augustExpense);


        mockMvc.perform(
                        get("/api/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .param(
                                        "start",
                                        "2026-08-01"
                                )
                                .param(
                                        "end",
                                        "2026-08-31"
                                )
                )
                .andExpect(status().isOk())

                // Only August transaction should appear
                .andExpect(
                        jsonPath("$", hasSize(1))
                )

                .andExpect(
                        jsonPath("$[0].description")
                                .value(
                                        "August transaction"
                                )
                )

                .andExpect(
                        jsonPath("$[0].amount")
                                .value(9000.00)
                );
    }


    // =========================================================
    // EMPTY DATE RANGE
    // =========================================================

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist()
            throws Exception {

        mockMvc.perform(
                        get("/api/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .param(
                                        "start",
                                        "2026-09-01"
                                )
                                .param(
                                        "end",
                                        "2026-09-30"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$").isArray()
                )
                .andExpect(
                        jsonPath("$", hasSize(0))
                );
    }


    // =========================================================
    // UNAUTHENTICATED REQUEST
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedRequest()
            throws Exception {

        mockMvc.perform(
                        get("/api/transactions")
                                .param(
                                        "start",
                                        "2026-08-01"
                                )
                                .param(
                                        "end",
                                        "2026-08-31"
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

// =========================================================
// MISSING START DATE
// =========================================================

    @Test
    void shouldRejectRequestWhenStartDateIsMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .param(
                                        "end",
                                        "2026-08-31"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Required parameter 'start' is missing"
                                )
                );
    }


// =========================================================
// MISSING END DATE
// =========================================================

    @Test
    void shouldRejectRequestWhenEndDateIsMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .param(
                                        "start",
                                        "2026-08-01"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Required parameter 'end' is missing"
                                )
                );
    }
}