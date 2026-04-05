package com.tanalytics.privacy.dto;

import com.tanalytics.privacy.model.DeletionStatus;

import java.time.Instant;
import java.util.UUID;

public record DeletionRequestResponse(
        UUID id,
        UUID siteId,
        String visitorId,
        DeletionStatus status,
        String errorMessage,
        Instant requestedAt,
        Instant completedAt
) {}
