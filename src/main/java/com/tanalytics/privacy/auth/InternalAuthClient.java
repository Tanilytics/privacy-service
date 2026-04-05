package com.tanalytics.privacy.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
public class InternalAuthClient {

    private final RestClient restClient;
    private final InternalServiceTokenProvider tokenProvider;

    public InternalAuthClient(
            RestClient.Builder restClientBuilder,
            InternalServiceTokenProvider tokenProvider,
            @Value("${auth.service.base-url:http://localhost:8082}") String authServiceBaseUrl,
            @Value("${auth.service.timeout-ms:1500}") long timeoutMs
    ) {
        this.tokenProvider = tokenProvider;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

        this.restClient = restClientBuilder
                .baseUrl(authServiceBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isMember(UUID siteId, UUID userId) {
        try {
            InternalMembershipResponse response = restClient.get()
                    .uri("/internal/v1/auth/sites/{siteId}/users/{userId}/membership", siteId, userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.generateToken())
                    .retrieve()
                    .body(InternalMembershipResponse.class);

            return response != null && response.allowed();
        } catch (RestClientException ex) {
            throw new InternalAuthUnavailableException("Failed to verify membership with auth-service", ex);
        }
    }

    public InternalSiteConfigResponse getSiteConfig(UUID siteId) {
        try {
            return restClient.get()
                    .uri("/internal/v1/auth/sites/{siteId}/config", siteId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.generateToken())
                    .retrieve()
                    .body(InternalSiteConfigResponse.class);
        } catch (RestClientException ex) {
            throw new InternalAuthUnavailableException("Failed to fetch site config from auth-service", ex);
        }
    }

    public List<InternalSiteConfigResponse> getAllSiteConfigs() {
        try {
            InternalSiteConfigResponse[] response = restClient.get()
                    .uri("/internal/v1/auth/sites/configs")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.generateToken())
                    .retrieve()
                    .body(InternalSiteConfigResponse[].class);
            return response == null ? List.of() : List.of(response);
        } catch (RestClientException ex) {
            throw new InternalAuthUnavailableException("Failed to fetch site configs from auth-service", ex);
        }
    }
}
