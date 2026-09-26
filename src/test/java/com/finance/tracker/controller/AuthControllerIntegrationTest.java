package com.finance.tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.RegisterRequest;
import com.finance.tracker.repository.BudgetRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private NotificationRepository notificationRepository;


    // =========================================================
    // CLEAN DATABASE BEFORE EACH TEST
    // =========================================================

    @BeforeEach
    void cleanDatabase() {

        /*
         * Delete dependent records first.
         *
         * Notifications reference users.
         * Expenses reference users/categories.
         * Budgets reference users/categories.
         * Categories belong to users.
         *
         * Therefore, clean from children → parents.
         */

        notificationRepository.deleteAll();

        expenseRepository.deleteAll();

        budgetRepository.deleteAll();

        categoryRepository.deleteAll();

        userRepository.deleteAll();
    }


    // =========================================================
    // REGISTER
    // =========================================================

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setEmail("integration@test.com");
        request.setPassword("Password123!");
        request.setFirstName("Integration");
        request.setLastName("Test");
        request.setPhoneNumber("08012345678");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "User registered successfully"
                ));
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @Test
    void shouldLoginSuccessfullyAndReturnJwt() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest();

        registerRequest.setEmail("login@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("Login");
        registerRequest.setLastName("Test");
        registerRequest.setPhoneNumber("08012345678");

        // Register user first
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

        AuthRequest loginRequest = new AuthRequest();

        loginRequest.setEmail("login@test.com");
        loginRequest.setPassword("Password123!");

        // Login
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
                .andExpect(jsonPath("$.token").isNotEmpty());
    }


    // =========================================================
    // DUPLICATE EMAIL
    // =========================================================

    @Test
    void shouldRejectDuplicateEmail() throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setEmail("duplicate@test.com");
        request.setPassword("Password123!");
        request.setFirstName("First");
        request.setLastName("User");
        request.setPhoneNumber("08012345678");

        // First registration
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        // Second registration with same email
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("Email already in use"));
    }


    // =========================================================
    // WRONG PASSWORD
    // =========================================================

    @Test
    void shouldRejectInvalidLogin() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest();

        registerRequest.setEmail("wrongpassword@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFirstName("Wrong");
        registerRequest.setLastName("Password");
        registerRequest.setPhoneNumber("08012345678");

        // Register user
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

        AuthRequest loginRequest = new AuthRequest();

        loginRequest.setEmail("wrongpassword@test.com");
        loginRequest.setPassword("WrongPassword123!");

        // Attempt login with wrong password
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password."));
    }
}