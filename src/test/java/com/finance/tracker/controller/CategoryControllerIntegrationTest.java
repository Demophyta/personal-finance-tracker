package com.finance.tracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private static final String EMAIL =
            "category.integration@test.com";

    private static final String PASSWORD =
            "Password123!";


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest();

        registerRequest.setEmail(EMAIL);
        registerRequest.setPassword(PASSWORD);
        registerRequest.setFirstName("Category");
        registerRequest.setLastName("Integration");
        registerRequest.setPhoneNumber("08011111111");

        /*
         * Registration may return 200 or 500 if this user
         * already exists from a previous test run.
         *
         * We don't assert registration here.
         * We only need the user to exist so that login works.
         */
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        registerRequest
                                )
                        )
        );

        AuthRequest loginRequest = new AuthRequest();

        loginRequest.setEmail(EMAIL);
        loginRequest.setPassword(PASSWORD);

        String response = mockMvc.perform(
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

        JsonNode jsonNode = objectMapper.readTree(response);

        jwtToken = jsonNode.get("token").asText();
    }


    // =========================================================
    // CREATE CATEGORY
    // =========================================================

    @Test
    void shouldCreateCategorySuccessfully() throws Exception {

        String request = """
                {
                    "name": "Integration Food Test"
                }
                """;

        mockMvc.perform(
                        post("/api/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").exists())
                .andExpect(jsonPath("$.name")
                        .value("Integration Food Test"));
    }


    // =========================================================
    // GET CATEGORIES
    // =========================================================

    @Test
    void shouldGetUserCategoriesSuccessfully() throws Exception {

        String request = """
                {
                    "name": "Integration Transport Test"
                }
                """;

        mockMvc.perform(
                        post("/api/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(
                        jsonPath(
                                "$[?(@.name == 'Integration Transport Test')]"
                        ).exists()
                );
    }


    // =========================================================
    // UPDATE CATEGORY
    // =========================================================

    @Test
    void shouldUpdateCategorySuccessfully() throws Exception {

        String createRequest = """
                {
                    "name": "Old Category Integration Test"
                }
                """;

        String response = mockMvc.perform(
                        post("/api/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode category =
                objectMapper.readTree(response);

        Long categoryId =
                category.get("categoryId").asLong();


        String updateRequest = """
                {
                    "name": "Updated Category Integration Test"
                }
                """;

        mockMvc.perform(
                        put("/api/categories/" + categoryId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId")
                        .value(categoryId))
                .andExpect(jsonPath("$.name")
                        .value("Updated Category Integration Test"));
    }


    // =========================================================
    // DELETE CATEGORY
    // =========================================================

    @Test
    void shouldDeleteCategorySuccessfully() throws Exception {

        String createRequest = """
                {
                    "name": "Category Delete Integration Test"
                }
                """;

        String response = mockMvc.perform(
                        post("/api/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode category =
                objectMapper.readTree(response);

        Long categoryId =
                category.get("categoryId").asLong();


        mockMvc.perform(
                        delete("/api/categories/" + categoryId)
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Category deleted"));


        // Verify category is no longer returned
        mockMvc.perform(
                        get("/api/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$[?(@.categoryId == "
                                        + categoryId
                                        + ")]"
                        ).doesNotExist()
                );
    }


    // =========================================================
    // UNAUTHENTICATED CREATE
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedCreateCategory() throws Exception {

        String request = """
                {
                    "name": "Unauthorized Category Test"
                }
                """;

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // UNAUTHENTICATED GET
    // =========================================================

    @Test
    void shouldRejectUnauthenticatedGetCategories() throws Exception {

        mockMvc.perform(
                        get("/api/categories")
                )
                .andExpect(status().isUnauthorized());
    }
}