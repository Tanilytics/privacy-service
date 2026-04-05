package com.tanalytics.privacy.controller;

import com.tanalytics.privacy.auth.InternalAuthClient;
import com.tanalytics.privacy.auth.InternalAuthUnavailableException;
import com.tanalytics.privacy.config.SecurityConfig;
import com.tanalytics.privacy.dto.ConsentStatsResponse;
import com.tanalytics.privacy.dto.DeletionRequestResponse;
import com.tanalytics.privacy.dto.ExportResponse;
import com.tanalytics.privacy.model.DeletionStatus;
import com.tanalytics.privacy.security.JwtAuthFilter;
import com.tanalytics.privacy.security.JwtService;
import com.tanalytics.privacy.service.ConsentService;
import com.tanalytics.privacy.service.DeletionService;
import com.tanalytics.privacy.service.ExportService;
import com.tanalytics.privacy.service.SaltService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PrivacyController.class, InternalPrivacyController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "jwt.secret=this-is-a-test-secret-with-at-least-32-bytes!!"
})
class PrivacyControllerSecurityTest {

    private static final String SECRET = "this-is-a-test-secret-with-at-least-32-bytes!!";
    private static final String SITE_A = "11111111-1111-1111-1111-111111111111";
    private static final String USER_ID = "33333333-3333-3333-3333-333333333333";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InternalAuthClient internalAuthClient;

        @MockBean
        private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private DeletionService deletionService;

    @MockBean
    private ConsentService consentService;

    @MockBean
    private ExportService exportService;

    @MockBean
    private SaltService saltService;

    @Test
    void allowsAuthorizedDeletionRequest() throws Exception {
        when(internalAuthClient.isMember(UUID.fromString(SITE_A), UUID.fromString(USER_ID))).thenReturn(true);
        when(deletionService.requestDeletion(any(UUID.class), any(String.class)))
                .thenReturn(new DeletionRequestResponse(
                        UUID.randomUUID(),
                        UUID.fromString(SITE_A),
                        "visitor-1",
                        DeletionStatus.PENDING,
                        null,
                        Instant.now(),
                        null
                ));

        mockMvc.perform(post("/api/v1/sites/" + SITE_A + "/privacy/delete")
                        .header("Authorization", "Bearer " + token("access", List.of(SITE_A)))
                        .contentType("application/json")
                        .content("{\"visitorId\":\"visitor-1\"}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void rejectsWhenUserNotMember() throws Exception {
        when(internalAuthClient.isMember(UUID.fromString(SITE_A), UUID.fromString(USER_ID))).thenReturn(false);

        mockMvc.perform(get("/api/v1/sites/" + SITE_A + "/privacy/consent-stats")
                        .header("Authorization", "Bearer " + token("access", List.of(SITE_A))))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsServiceUnavailableWhenMembershipDependencyFails() throws Exception {
        when(internalAuthClient.isMember(any(UUID.class), any(UUID.class)))
                .thenThrow(new InternalAuthUnavailableException("unavailable", null));

        mockMvc.perform(get("/api/v1/sites/" + SITE_A + "/privacy/deletions")
                        .header("Authorization", "Bearer " + token("access", List.of(SITE_A))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void rejectsRefreshTokenOnPrivacyEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/sites/" + SITE_A + "/privacy/deletions")
                        .header("Authorization", "Bearer " + token("refresh", List.of(SITE_A))))
                .andExpect(status().isUnauthorized());
    }

    private String token(String type, List<String> siteIds) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(USER_ID)
                .claim("type", type)
                .claim("role", "admin")
                .claim("siteIds", siteIds)
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }
}
