package com.finance.tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.RegisterRequest;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.User;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String jwtToken;

    private Long userId;

    private Long foodCategoryId;
    private Long transportCategoryId;
    private Long shoppingCategoryId;
    private Long entertainmentCategoryId;

    private static final String PASSWORD =
            "Password123!";


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Every test gets a unique user.
         *
         * This prevents duplicate email/category conflicts
         * between test runs.
         */

        String uniqueEmail =
                "dashboard." + System.nanoTime() + "@test.com";


        // -----------------------------------------------------
        // REGISTER
        // -----------------------------------------------------

        RegisterRequest registerRequest =
                new RegisterRequest();

        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setFirstName("Dashboard");
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

        AuthRequest loginRequest =
                new AuthRequest();

        loginRequest.setEmail(uniqueEmail);
        loginRequest.setPassword(PASSWORD);

        String loginResponse =
                mockMvc.perform(
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

        User user =
                userRepository.findByEmail(uniqueEmail)
                        .orElseThrow();

        userId =
                user.getUserId();


        // -----------------------------------------------------
        // CREATE CATEGORIES
        // -----------------------------------------------------

        foodCategoryId =
                createCategory(
                        user,
                        "Dashboard Food " + System.nanoTime()
                );

        transportCategoryId =
                createCategory(
                        user,
                        "Dashboard Transport " + System.nanoTime()
                );

        shoppingCategoryId =
                createCategory(
                        user,
                        "Dashboard Shopping " + System.nanoTime()
                );

        entertainmentCategoryId =
                createCategory(
                        user,
                        "Dashboard Entertainment " + System.nanoTime()
                );
    }


    // =========================================================
    // CATEGORY HELPER
    // =========================================================

    private Long createCategory(
            User user,
            String name) {

        Category category =
                Category.builder()
                        .name(name)
                        .user(user)
                        .build();

        return categoryRepository
                .save(category)
                .getCategoryId();
    }


    // =========================================================
    // INCOME HELPER
    // =========================================================

    private void createIncome(
            double amount) throws Exception {

        String request = """
                {
                    "amount": %s,
                    "date": "%s",
                    "description": "Dashboard test income",
                    "type": "INCOME",
                    "categoryId": %d
                }
                """.formatted(
                amount,
                LocalDate.now(),
                foodCategoryId
        );

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());
    }


    // =========================================================
    // EXPENSE HELPER
    // =========================================================

    private void createExpense(
            double amount,
            Long categoryId,
            String description) throws Exception {

        String request = """
                {
                    "amount": %s,
                    "date": "%s",
                    "description": "%s",
                    "type": "EXPENSE",
                    "categoryId": %d
                }
                """.formatted(
                amount,
                LocalDate.now(),
                description,
                categoryId
        );

        mockMvc.perform(
                        post("/api/expenses/" + userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());
    }


    // =========================================================
    // GET DASHBOARD
    // =========================================================

    @Test
    void shouldGetDashboardSuccessfully() throws Exception {

        /*
         * Income:
         * 100,000
         */
        createIncome(100000.00);


        /*
         * Food:
         * 30,000 + 10,000 = 40,000
         */
        createExpense(
                30000.00,
                foodCategoryId,
                "Food expense one"
        );

        createExpense(
                10000.00,
                foodCategoryId,
                "Food expense two"
        );


        /*
         * Transport:
         * 20,000 + 5,000 = 25,000
         */
        createExpense(
                20000.00,
                transportCategoryId,
                "Transport expense one"
        );

        createExpense(
                5000.00,
                transportCategoryId,
                "Transport expense two"
        );


        /*
         * Shopping:
         * 15,000
         */
        createExpense(
                15000.00,
                shoppingCategoryId,
                "Shopping expense"
        );


        /*
         * Entertainment:
         * 5,000
         */
        createExpense(
                5000.00,
                entertainmentCategoryId,
                "Entertainment expense"
        );


        /*
         * Total expenses:
         *
         * Food          = 40,000
         * Transport     = 25,000
         * Shopping      = 15,000
         * Entertainment = 5,000
         *
         * Total = 85,000
         *
         * Income = 100,000
         *
         * Net balance = 15,000
         */

        mockMvc.perform(
                        get("/api/dashboard")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())


                // -------------------------------------------------
                // FINANCIAL SUMMARY
                // -------------------------------------------------

                .andExpect(
                        jsonPath("$.totalIncome")
                                .value(100000.00)
                )

                .andExpect(
                        jsonPath("$.totalExpenses")
                                .value(85000.00)
                )

                .andExpect(
                        jsonPath("$.netBalance")
                                .value(15000.00)
                )


                // -------------------------------------------------
                // TOP CATEGORIES
                // -------------------------------------------------

                .andExpect(
                        jsonPath("$.topCategories")
                                .isArray()
                )

                .andExpect(
                        jsonPath("$.topCategories")
                                .value(hasSize(3))
                )


                // First category = Food
                .andExpect(
                        jsonPath(
                                "$.topCategories[0].categoryName"
                        )
                                .value(
                                        startsWith(
                                                "Dashboard Food"
                                        )
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.topCategories[0].totalSpent"
                        )
                                .value(40000.00)
                )


                // Second category = Transport
                .andExpect(
                        jsonPath(
                                "$.topCategories[1].categoryName"
                        )
                                .value(
                                        startsWith(
                                                "Dashboard Transport"
                                        )
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.topCategories[1].totalSpent"
                        )
                                .value(25000.00)
                )


                // Third category = Shopping
                .andExpect(
                        jsonPath(
                                "$.topCategories[2].categoryName"
                        )
                                .value(
                                        startsWith(
                                                "Dashboard Shopping"
                                        )
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.topCategories[2].totalSpent"
                        )
                                .value(15000.00)
                )


                // -------------------------------------------------
                // REMAINING BUDGET
                // -------------------------------------------------

                .andExpect(
                        jsonPath("$.remainingBudget")
                                .value(0.00)
                );
    }


    // =========================================================
    // EMPTY DASHBOARD
    // =========================================================

    @Test
    void shouldReturnEmptyDashboardForUserWithNoTransactions()
            throws Exception {

        mockMvc.perform(
                        get("/api/dashboard")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.totalIncome")
                                .value(0.00)
                )

                .andExpect(
                        jsonPath("$.totalExpenses")
                                .value(0.00)
                )

                .andExpect(
                        jsonPath("$.netBalance")
                                .value(0.00)
                )

                .andExpect(
                        jsonPath("$.topCategories")
                                .isArray()
                )

                .andExpect(
                        jsonPath("$.topCategories")
                                .isEmpty()
                )

                .andExpect(
                        jsonPath("$.remainingBudget")
                                .value(0.00)
                );
    }


    // =========================================================
    // TOP THREE CATEGORIES
    // =========================================================

    @Test
    void shouldReturnOnlyTopThreeSpendingCategories()
            throws Exception {

        createIncome(200000.00);


        // Food = 50,000
        createExpense(
                50000.00,
                foodCategoryId,
                "Food"
        );


        // Transport = 40,000
        createExpense(
                40000.00,
                transportCategoryId,
                "Transport"
        );


        // Shopping = 30,000
        createExpense(
                30000.00,
                shoppingCategoryId,
                "Shopping"
        );


        // Entertainment = 20,000
        createExpense(
                20000.00,
                entertainmentCategoryId,
                "Entertainment"
        );


        mockMvc.perform(
                        get("/api/dashboard")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())

                /*
                 * DashboardService uses .limit(3)
                 */
                .andExpect(
                        jsonPath("$.topCategories")
                                .value(hasSize(3))
                )

                .andExpect(
                        jsonPath(
                                "$.topCategories[0].totalSpent"
                        )
                                .value(50000.00)
                )

                .andExpect(
                        jsonPath(
                                "$.topCategories[1].totalSpent"
                        )
                                .value(40000.00)
                )

                .andExpect(
                        jsonPath(
                                "$.topCategories[2].totalSpent"
                        )
                                .value(30000.00)
                );
    }


    // =========================================================
    // UNAUTHENTICATED DASHBOARD
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedDashboardAccess()
            throws Exception {

        mockMvc.perform(
                        get("/api/dashboard")
                )
                .andExpect(status().isUnauthorized());
    }
}