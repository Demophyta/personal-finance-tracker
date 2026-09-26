package com.finance.tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI financeTrackerOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Add JWT security scheme for all endpoints
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // API general information
                .info(new Info()
                        .title("Finance Tracker API")
                        .version("1.0.0")
                        .description("REST API documentation for the Finance Tracker Application. " +
                                "This application allows users to manage expenses, track budgets, " +
                                "and analyze financial activities efficiently.")
                        .contact(new Contact()
                                .name("Finance Tracker Team")
                                .email("support@financetracker.com")
                                .url("https://financetracker.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
