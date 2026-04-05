package com.tanalytics.privacy.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDeletionRequest(
        @NotBlank String visitorId
) {}
