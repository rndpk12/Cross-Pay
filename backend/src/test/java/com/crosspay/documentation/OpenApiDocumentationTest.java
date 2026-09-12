package com.crosspay.documentation;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class OpenApiDocumentationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("jwt.secret", () -> "test-only-secret-must-be-at-least-32-bytes");
        registry.add("jwt.expiration", () -> "900000");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesDocumentedApiWithBearerSecurityAndPublicAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Cross Pay API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse").exists())
                .andExpect(jsonPath("$.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/register'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/fx/quotes'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/transfers'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/deposits'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/withdrawals'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/transactions'].get").exists());
    }

    @Test
    void exposesSwaggerUiRedirect() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void exposesSafeHealthAndKeepsManagementAndFinancialRoutesProtected() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details").doesNotExist())
                .andExpect(header().exists("X-Request-Id"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details").doesNotExist());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/deposits"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void preservesValidRequestCorrelationId() throws Exception {
        mockMvc.perform(get("/v3/api-docs").header("X-Request-Id", "deploy-42.request_1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "deploy-42.request_1"));
    }

    @Test
    void rejectsMalformedAndInvalidJwtTokensWithoutExposingDetails() throws Exception {
        SecretKey validKey = Keys.hmacShaKeyFor(
                "test-only-secret-must-be-at-least-32-bytes".getBytes(StandardCharsets.UTF_8)
        );
        String expired = signedToken(validKey, UUID.randomUUID().toString(), new Date(System.currentTimeMillis() - 1_000));
        String invalidSubject = signedToken(validKey, "not-a-uuid", new Date(System.currentTimeMillis() + 60_000));
        SecretKey invalidKey = Keys.hmacShaKeyFor(
                "another-test-secret-must-be-at-least-32-bytes".getBytes(StandardCharsets.UTF_8)
        );
        String invalidSignature = signedToken(invalidKey, UUID.randomUUID().toString(), new Date(System.currentTimeMillis() + 60_000));

        for (String token : new String[]{"not-a-jwt", expired, invalidSubject, invalidSignature}) {
            mockMvc.perform(get("/api/v1/wallets").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("Authentication is required"));
        }
    }

    @Test
    void sendsBrowserSecurityHeadersOnPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }

    @Test
    void rejectsUnconfiguredCrossOriginBrowserRequestsByDefault() throws Exception {
        mockMvc.perform(options("/api/v1/wallets")
                        .header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    private String signedToken(SecretKey key, String subject, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
