package com.tanalytics.privacy.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ExportResponse(
        UUID siteId,
        String visitorId,
        Instant generatedAt,
        int eventCount,
        List<Map<String, Object>> events
) {}
