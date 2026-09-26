package com.finance.tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.RegisterRequest;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.RecurringTransactionRepository;
import com.finance.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RecurringTransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

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
         * Every test gets a new user.
         *
         * This prevents test data from previous tests from
         * interfering with the current test.
         */

        String uniqueEmail =
                "recurring." + System.nanoTime() + "@test.com";


        // -----------------------------------------------------
        // REGISTER
        // -----------------------------------------------------

        RegisterRequest registerRequest = new RegisterRequest();

        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setFirstName("Recurring");
        registerRequest.setLastName("Integration");
        registerRequest.setPhoneNumber("08044444444");


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
        // CREATE CATEGORY
        // -----------------------------------------------------

        Category category = Category.builder()
                .name("Recurring Test Category " + System.nanoTime())
                .user(user)
                .build();

        categoryId =
                categoryRepository.save(category)
                        .getCategoryId();
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void shouldCreateRecurringTransactionSuccessfully()
            throws Exception {

        String request = """
                {
                    "categoryId": %d,
                    "amount": 15000.00,
                    "type": "EXPENSE",
                    "frequency": "MONTHLY",
                    "startDate": "2026-08-01",
                    "endDate": "2026-12-31"
                }
                """.formatted(categoryId);


        mockMvc.perform(
                        post("/api/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurringTransactionId")
                        .exists())
                .andExpect(jsonPath("$.categoryName")
                        .value(startsWith(
                                "Recurring Test Category"
                        )))
                .andExpect(jsonPath("$.amount")
                        .value(15000.00))
                .andExpect(jsonPath("$.type")
                        .value("EXPENSE"))
                .andExpect(jsonPath("$.frequency")
                        .value("MONTHLY"))
                .andExpect(jsonPath("$.startDate")
                        .value("2026-08-01"))
                .andExpect(jsonPath("$.endDate")
                        .value("2026-12-31"))
                .andExpect(jsonPath("$.active")
                        .value(true));
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void shouldGetRecurringTransactionsSuccessfully()
            throws Exception {

        String request = """
                {
                    "categoryId": %d,
                    "amount": 10000.00,
                    "type": "EXPENSE",
                    "frequency": "MONTHLY",
                    "startDate": "2026-08-01",
                    "endDate": "2026-12-31"
                }
                """.formatted(categoryId);


        mockMvc.perform(
                post("/api/recurring-transactions")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isCreated());


        mockMvc.perform(
                        get("/api/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].recurringTransactionId")
                        .exists())
                .andExpect(jsonPath("$[0].categoryName")
                        .value(startsWith(
                                "Recurring Test Category"
                        )))
                .andExpect(jsonPath("$[0].amount")
                        .value(10000.00))
                .andExpect(jsonPath("$[0].type")
                        .value("EXPENSE"))
                .andExpect(jsonPath("$[0].frequency")
                        .value("MONTHLY"))
                .andExpect(jsonPath("$[0].active")
                        .value(true));
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void shouldUpdateRecurringTransactionSuccessfully()
            throws Exception {

        String createRequest = """
                {
                    "categoryId": %d,
                    "amount": 10000.00,
                    "type": "EXPENSE",
                    "frequency": "MONTHLY",
                    "startDate": "2026-08-01",
                    "endDate": "2026-12-31"
                }
                """.formatted(categoryId);


        String createResponse = mockMvc.perform(
                        post("/api/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode created =
                objectMapper.readTree(createResponse);

        Long recurringTransactionId =
                created.get("recurringTransactionId").asLong();


        // -----------------------------------------------------
        // UPDATE
        // -----------------------------------------------------

        String updateRequest = """
                {
                    "categoryId": %d,
                    "amount": 25000.00,
                    "type": "EXPENSE",
                    "frequency": "WEEKLY",
                    "startDate": "2026-08-05",
                    "endDate": "2026-12-31"
                }
                """.formatted(categoryId);


        mockMvc.perform(
                        put(
                                "/api/recurring-transactions/"
                                        + recurringTransactionId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recurringTransactionId")
                        .value(recurringTransactionId))
                .andExpect(jsonPath("$.amount")
                        .value(25000.00))
                .andExpect(jsonPath("$.type")
                        .value("EXPENSE"))
                .andExpect(jsonPath("$.frequency")
                        .value("WEEKLY"))
                .andExpect(jsonPath("$.startDate")
                        .value("2026-08-05"))
                .andExpect(jsonPath("$.endDate")
                        .value("2026-12-31"))
                .andExpect(jsonPath("$.active")
                        .value(true));
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void shouldDeleteRecurringTransactionSuccessfully()
            throws Exception {

        String createRequest = """
                {
                    "categoryId": %d,
                    "amount": 8000.00,
                    "type": "EXPENSE",
                    "frequency": "MONTHLY",
                    "startDate": "2026-08-01",
                    "endDate": "2026-12-31"
                }
                """.formatted(categoryId);


        String createResponse = mockMvc.perform(
                        post("/api/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode created =
                objectMapper.readTree(createResponse);

        Long recurringTransactionId =
                created.get("recurringTransactionId").asLong();


        // -----------------------------------------------------
        // DELETE
        // -----------------------------------------------------

        mockMvc.perform(
                        delete(
                                "/api/recurring-transactions/"
                                        + recurringTransactionId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "Recurring transaction deleted successfully"
                        )
                );


        // -----------------------------------------------------
        // VERIFY DATABASE
        // -----------------------------------------------------

        org.junit.jupiter.api.Assertions.assertFalse(
                recurringTransactionRepository
                        .findById(recurringTransactionId)
                        .isPresent()
        );
    }


    // =========================================================
    // GET AFTER DELETE
    // =========================================================

    @Test
    void shouldNotReturnDeletedRecurringTransaction()
            throws Exception {

        String createRequest = """
                {
                    "categoryId": %d,
                    "amount": 7000.00,
                    "type": "EXPENSE",
                    "frequency": "MONTHLY",
                    "startDate": "2026-08-01",
                    "endDate": "2026-12-31"
                }
                """.formatted(categoryId);


        String createResponse = mockMvc.perform(
                        post("/api/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode created =
                objectMapper.readTree(createResponse);

        Long recurringTransactionId =
                created.get("recurringTransactionId").asLong();


        mockMvc.perform(
                delete(
                        "/api/recurring-transactions/"
                                + recurringTransactionId
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        ).andExpect(status().isOk());


        mockMvc.perform(
                        get("/api/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(
                        jsonPath(
                                "$[?(@.recurringTransactionId == %d)]"
                                        .formatted(
                                                recurringTransactionId
                                        )
                        ).doesNotExist()
                );
    }


    // =========================================================
    // UNAUTHENTICATED GET
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedGet()
            throws Exception {

        mockMvc.perform(
                        get("/api/recurring-transactions")
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED CREATE
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedCreate()
            throws Exception {

        String request = """
                {
                    "categoryId": %d,
                    "amount": 10000.00,
                    "type": "EXPENSE",
                    "frequency": "MONTHLY",
                    "startDate": "2026-08-01",
                    "endDate": "2026-12-31"
                }
                """.formatted(categoryId);


        mockMvc.perform(
                        post("/api/recurring-transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED UPDATE
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedUpdate()
            throws Exception {

        String request = """
                {
                    "categoryId": %d,
                    "amount": 20000.00,
                    "type": "EXPENSE",
                    "frequency": "MONTHLY",
                    "startDate": "2026-08-01",
                    "endDate": "2026-12-31"
                }
                """.formatted(categoryId);


        mockMvc.perform(
                        put("/api/recurring-transactions/999999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED DELETE
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedDelete()
            throws Exception {

        mockMvc.perform(
                        delete("/api/recurring-transactions/999999")
                )
                .andExpect(status().isUnauthorized());
    }
}