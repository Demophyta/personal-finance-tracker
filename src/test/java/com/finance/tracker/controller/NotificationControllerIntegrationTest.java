package com.finance.tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.RegisterRequest;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.Expense;
import com.finance.tracker.model.Notification;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.NotificationRepository;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class NotificationControllerIntegrationTest {

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

    @Autowired
    private NotificationRepository notificationRepository;

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
         * Every test gets a unique user.
         *
         * This prevents conflicts with users, categories,
         * expenses and notifications created by previous tests.
         */

        String uniqueEmail =
                "notification."
                        + System.nanoTime()
                        + "@test.com";


        RegisterRequest registerRequest =
                new RegisterRequest();

        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setFirstName("Notification");
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

        userId =
                user.getUserId();


        // -----------------------------------------------------
        // CREATE TEST CATEGORY
        // -----------------------------------------------------

        Category category =
                Category.builder()
                        .name(
                                "Notification Test Category "
                                        + System.nanoTime()
                        )
                        .user(user)
                        .build();

        categoryId =
                categoryRepository.save(category)
                        .getCategoryId();
    }


    // =========================================================
    // GET NOTIFICATIONS
    // =========================================================

    @Test
    void shouldGetNotificationsSuccessfully()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();


        Notification notification =
                Notification.builder()
                        .user(user)
                        .message(
                                "Integration test notification"
                        )
                        .createdAt(LocalDateTime.now())
                        .read(false)
                        .build();

        notificationRepository.save(notification);


        mockMvc.perform(
                        get("/api/notifications")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(
                        jsonPath("$[0].id")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[0].message")
                                .value(
                                        "Integration test notification"
                                )
                )
                .andExpect(
                        jsonPath("$[0].read")
                                .value(false)
                );
    }


    // =========================================================
    // EXPENSE CREATES NOTIFICATION
    // =========================================================

    @Test
    void shouldCreateNotificationWhenExpenseIsCreated()
            throws Exception {

        /*
         * The expense service requires sufficient balance
         * before creating an EXPENSE.
         *
         * Therefore create income first.
         */

        String incomeRequest = """
                {
                    "amount": 100000.00,
                    "date": "2026-08-20",
                    "description": "Notification test income",
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
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(incomeRequest)
                )
                .andExpect(status().isCreated());


        /*
         * Create the actual expense.
         */

        String expenseRequest = """
                {
                    "amount": 25000.00,
                    "date": "2026-08-20",
                    "description": "Notification test expense",
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
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(expenseRequest)
                )
                .andExpect(status().isCreated());


        /*
         * The expense should have created a notification.
         */

        mockMvc.perform(
                        get("/api/notifications")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(
                        jsonPath("$[?(@.message =~ /.*Expense.*recorded.*/)]")
                                .exists()
                );
    }


    // =========================================================
    // INCOME CREATES NOTIFICATION
    // =========================================================

    @Test
    void shouldCreateNotificationWhenIncomeIsCreated()
            throws Exception {

        String incomeRequest = """
                {
                    "amount": 50000.00,
                    "date": "2026-08-20",
                    "description": "Salary income",
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
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(incomeRequest)
                )
                .andExpect(status().isCreated());


        /*
         * Verify the notification through the API.
         */

        mockMvc.perform(
                        get("/api/notifications")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(
                        jsonPath("$[0].message")
                                .value(
                                        containsString(
                                                "Income of"
                                        )
                                )
                )
                .andExpect(
                        jsonPath("$[0].read")
                                .value(false)
                );
    }


    // =========================================================
    // MARK NOTIFICATION AS READ
    // =========================================================

    @Test
    void shouldMarkNotificationAsRead()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();


        Notification notification =
                Notification.builder()
                        .user(user)
                        .message(
                                "Notification to mark as read"
                        )
                        .createdAt(LocalDateTime.now())
                        .read(false)
                        .build();

        Notification saved =
                notificationRepository.save(notification);


        /*
         * Mark notification as read.
         */

        mockMvc.perform(
                        post(
                                "/api/notifications/"
                                        + saved.getId()
                                        + "/read"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "Notification marked as read"
                        )
                );


        /*
         * Verify database state.
         */

        Notification updated =
                notificationRepository
                        .findById(saved.getId())
                        .orElseThrow();

        org.junit.jupiter.api.Assertions.assertTrue(
                updated.isRead()
        );
    }


    // =========================================================
    // UNAUTHENTICATED GET
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedGetNotifications()
            throws Exception {

        mockMvc.perform(
                        get("/api/notifications")
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED MARK AS READ
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedMarkAsRead()
            throws Exception {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();


        Notification notification =
                Notification.builder()
                        .user(user)
                        .message(
                                "Protected notification"
                        )
                        .createdAt(LocalDateTime.now())
                        .read(false)
                        .build();

        Notification saved =
                notificationRepository.save(notification);


        mockMvc.perform(
                        post(
                                "/api/notifications/"
                                        + saved.getId()
                                        + "/read"
                        )
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // ONLY CURRENT USER'S NOTIFICATIONS
    // =========================================================

    @Test
    void shouldReturnOnlyAuthenticatedUsersNotifications()
            throws Exception {

        User currentUser =
                userRepository.findById(userId)
                        .orElseThrow();


        /*
         * Create a notification for the authenticated user.
         */

        Notification ownNotification =
                Notification.builder()
                        .user(currentUser)
                        .message("Own notification")
                        .createdAt(LocalDateTime.now())
                        .read(false)
                        .build();

        notificationRepository.save(ownNotification);


        /*
         * Create another user.
         */

        String otherEmail =
                "other.notification."
                        + System.nanoTime()
                        + "@test.com";


        User otherUser =
                User.builder()
                        .email(otherEmail)
                        .firstName("Other")
                        .lastName("User")
                        .password(PASSWORD)
                        .phoneNumber("08055555555")
                        .build();

        otherUser =
                userRepository.save(otherUser);


        /*
         * Create notification belonging to the other user.
         */

        Notification otherNotification =
                Notification.builder()
                        .user(otherUser)
                        .message("Other user's notification")
                        .createdAt(LocalDateTime.now())
                        .read(false)
                        .build();

        notificationRepository.save(otherNotification);


        /*
         * Authenticated user should only see their own
         * notification.
         */

        mockMvc.perform(
                        get("/api/notifications")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(
                        jsonPath("$[0].message")
                                .value("Own notification")
                );
    }
}