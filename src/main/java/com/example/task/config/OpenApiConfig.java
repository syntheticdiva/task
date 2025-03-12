package com.example.task.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    private static final String SECURITY_SCHEME_TYPE = "bearer";
    private static final String SECURITY_BEARER_FORMAT = "JWT";
    private static final SecurityScheme.Type SECURITY_SCHEME_HTTP_TYPE = SecurityScheme.Type.HTTP;
    private static final String API_TITLE = "Task Management API";
    private static final String API_VERSION = "1.0";
    private static final String API_DESCRIPTION = "API Documentation for Task Management System";
    private static final String GROUP_NAME = "public-apis";
    private static final String PATH_PATTERN = "/**";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SECURITY_SCHEME_HTTP_TYPE)
                                        .scheme(SECURITY_SCHEME_TYPE)
                                        .bearerFormat(SECURITY_BEARER_FORMAT)
                        )
                )
                .info(new Info()
                        .title(API_TITLE)
                        .version(API_VERSION)
                        .description(API_DESCRIPTION)
                );
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group(GROUP_NAME)
                .pathsToMatch(PATH_PATTERN)
                .build();
    }
}