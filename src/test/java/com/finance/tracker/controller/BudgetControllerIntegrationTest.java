package com.finance.tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.RegisterRequest;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BudgetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /*
     * These repositories are only used to make sure the test data
     * belongs to the test setup when necessary.
     */
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private String jwtToken;
    private Long categoryId;

    private static final String PASSWORD = "Password123!";


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Generate a completely new email for every test.
         *
         * This prevents:
         *
         * Email already in use
         *
         * when tests are run repeatedly.
         */
        String email =
                "budget." + UUID.randomUUID() + "@test.com";


        // =====================================================
        // REGISTER USER
        // =====================================================

        RegisterRequest registerRequest =
                new RegisterRequest();

        registerRequest.setEmail(email);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setFirstName("Budget");
        registerRequest.setLastName("Integration");
        registerRequest.setPhoneNumber("08022222222");

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


        // =====================================================
        // LOGIN
        // =====================================================

        AuthRequest loginRequest =
                new AuthRequest();

        loginRequest.setEmail(email);
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


        // =====================================================
        // CREATE CATEGORY
        // =====================================================

        String categoryName =
                "Budget Category " + UUID.randomUUID();

        String categoryRequest = """
                {
                    "name": "%s"
                }
                """.formatted(categoryName);

        String categoryResponse =
                mockMvc.perform(
                                post("/api/categories")
                                        .header(
                                                "Authorization",
                                                "Bearer " + jwtToken
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(categoryRequest)
                        )
                        .andExpect(status().isOk())
                        .andExpect(
                                jsonPath("$.categoryId").exists()
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode categoryJson =
                objectMapper.readTree(categoryResponse);

        categoryId =
                categoryJson.get("categoryId").asLong();
    }


    // =========================================================
    // CREATE BUDGET
    // =========================================================

    @Test
    void shouldCreateBudgetSuccessfully() throws Exception {

        String request = """
                {
                    "categoryId": %d,
                    "amount": 50000,
                    "month": 8,
                    "year": 2026
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/budgets")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.budgetId").exists()
                )
                .andExpect(
                        jsonPath("$.categoryName").exists()
                )
                .andExpect(
                        jsonPath("$.amount").value(50000)
                )
                .andExpect(
                        jsonPath("$.month").value(8)
                )
                .andExpect(
                        jsonPath("$.year").value(2026)
                );
    }


    // =========================================================
    // GET USER BUDGETS
    // =========================================================

    @Test
    void shouldGetUserBudgetsSuccessfully() throws Exception {

        String request = """
                {
                    "categoryId": %d,
                    "amount": 50000,
                    "month": 8,
                    "year": 2026
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/budgets")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(status().isCreated());


        mockMvc.perform(
                        get("/api/budgets")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(
                        jsonPath("$[0].budgetId").exists()
                )
                .andExpect(
                        jsonPath("$[0].categoryName").exists()
                )
                .andExpect(
                        jsonPath("$[0].amount").value(50000)
                );
    }


    // =========================================================
    // UPDATE BUDGET
    // =========================================================

    @Test
    void shouldUpdateBudgetSuccessfully() throws Exception {

        String createRequest = """
                {
                    "categoryId": %d,
                    "amount": 50000,
                    "month": 8,
                    "year": 2026
                }
                """.formatted(categoryId);

        String response =
                mockMvc.perform(
                                post("/api/budgets")
                                        .header(
                                                "Authorization",
                                                "Bearer " + jwtToken
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(createRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode budgetJson =
                objectMapper.readTree(response);

        Long budgetId =
                budgetJson.get("budgetId").asLong();


        String updateRequest = """
                {
                    "categoryId": %d,
                    "amount": 75000,
                    "month": 8,
                    "year": 2026
                }
                """.formatted(categoryId);


        mockMvc.perform(
                        put("/api/budgets/" + budgetId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.budgetId")
                                .value(budgetId)
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(75000)
                )
                .andExpect(
                        jsonPath("$.month")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.year")
                                .value(2026)
                );
    }


    // =========================================================
    // DELETE BUDGET
    // =========================================================

    @Test
    void shouldDeleteBudgetSuccessfully() throws Exception {

        String createRequest = """
                {
                    "categoryId": %d,
                    "amount": 50000,
                    "month": 8,
                    "year": 2026
                }
                """.formatted(categoryId);

        String response =
                mockMvc.perform(
                                post("/api/budgets")
                                        .header(
                                                "Authorization",
                                                "Bearer " + jwtToken
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(createRequest)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode budgetJson =
                objectMapper.readTree(response);

        Long budgetId =
                budgetJson.get("budgetId").asLong();


        mockMvc.perform(
                        delete("/api/budgets/" + budgetId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "Budget deleted successfully."
                        )
                );


        // Verify it is gone
        mockMvc.perform(
                        get("/api/budgets")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$[?(@.budgetId == "
                                        + budgetId
                                        + ")]"
                        ).doesNotExist()
                );
    }


    // =========================================================
    // GET BUDGET USAGE
    // =========================================================

    @Test
    void shouldGetBudgetUsageSuccessfully() throws Exception {

        String request = """
                {
                    "categoryId": %d,
                    "amount": 50000,
                    "month": 8,
                    "year": 2026
                }
                """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/budgets")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(status().isCreated());


        mockMvc.perform(
                        get("/api/budgets/usage")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }


    // =========================================================
    // UNAUTHENTICATED CREATE
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedCreateBudget()
            throws Exception {

        String request = """
                {
                    "categoryId": 1,
                    "amount": 50000,
                    "month": 8,
                    "year": 2026
                }
                """;

        mockMvc.perform(
                        post("/api/budgets")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED GET
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedGetBudgets()
            throws Exception {

        mockMvc.perform(
                        get("/api/budgets")
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED USAGE
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedBudgetUsage()
            throws Exception {

        mockMvc.perform(
                        get("/api/budgets/usage")
                )
                .andExpect(status().isUnauthorized());
    }
}