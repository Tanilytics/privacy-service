package com.tanalytics.privacy.dto;

import com.tanalytics.privacy.model.ConsentScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateConsentRequest(
        @NotBlank String visitorId,
        boolean consentGiven,
        @NotNull ConsentScope scope,
        String ipHash
) {}
