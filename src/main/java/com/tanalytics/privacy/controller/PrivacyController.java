package com.tanalytics.privacy.controller;

import com.tanalytics.privacy.dto.ConsentStatsResponse;
import com.tanalytics.privacy.dto.CreateConsentRequest;
import com.tanalytics.privacy.dto.CreateDeletionRequest;
import com.tanalytics.privacy.dto.DeletionRequestResponse;
import com.tanalytics.privacy.dto.ExportResponse;
import com.tanalytics.privacy.model.DeletionStatus;
import com.tanalytics.privacy.service.ConsentService;
import com.tanalytics.privacy.service.DeletionService;
import com.tanalytics.privacy.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sites/{siteId}/privacy")
@Tag(name = "Privacy", description = "Privacy and GDPR endpoints")
public class PrivacyController {

    private final DeletionService deletionService;
    private final ConsentService consentService;
    private final ExportService exportService;

    public PrivacyController(
            DeletionService deletionService,
            ConsentService consentService,
            ExportService exportService
    ) {
        this.deletionService = deletionService;
        this.consentService = consentService;
        this.exportService = exportService;
    }

    @PostMapping("/delete")
    @Operation(summary = "Create a GDPR deletion request")
    public ResponseEntity<DeletionRequestResponse> requestDeletion(
            @PathVariable UUID siteId,
            @Valid @RequestBody CreateDeletionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(deletionService.requestDeletion(siteId, request.visitorId()));
    }

    @GetMapping("/deletions")
    @Operation(summary = "List deletion requests for a site")
    public ResponseEntity<List<DeletionRequestResponse>> listDeletions(
            @PathVariable UUID siteId,
            @RequestParam(required = false) DeletionStatus status,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(deletionService.listDeletions(siteId, status, limit));
    }

    @PostMapping("/consent")
    @Operation(summary = "Record a consent decision")
    public ResponseEntity<Void> recordConsent(
            @PathVariable UUID siteId,
            @Valid @RequestBody CreateConsentRequest request
    ) {
        consentService.recordConsent(siteId, request.visitorId(), request.consentGiven(), request.scope(), request.ipHash());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/consent-stats")
    @Operation(summary = "Get aggregated consent statistics")
    public ResponseEntity<ConsentStatsResponse> consentStats(
            @PathVariable UUID siteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        Instant resolvedTo = to == null ? Instant.now() : to;
        Instant resolvedFrom = from == null ? resolvedTo.minus(30, ChronoUnit.DAYS) : from;
        return ResponseEntity.ok(consentService.getConsentStats(siteId, resolvedFrom, resolvedTo));
    }

    @GetMapping("/export")
    @Operation(summary = "Export visitor data for GDPR access request")
    public ResponseEntity<ExportResponse> export(
            @PathVariable UUID siteId,
            @RequestParam String visitorId,
            @RequestParam(defaultValue = "5000") int limit
    ) {
        return ResponseEntity.ok(exportService.exportVisitorData(siteId, visitorId, limit));
    }
}
