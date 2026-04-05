package com.tanalytics.privacy.dto;

import java.time.LocalDate;
import java.util.UUID;

public record InternalSaltResponse(
        UUID siteId,
        LocalDate saltDate,
        String salt
) {}
