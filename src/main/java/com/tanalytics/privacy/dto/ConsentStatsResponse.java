package com.tanalytics.privacy.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ConsentStatsResponse(
        UUID siteId,
        Instant from,
        Instant to,
        long totalRecords,
        long grantedRecords,
        long deniedRecords,
        Map<String, Long> byScope
) {}
