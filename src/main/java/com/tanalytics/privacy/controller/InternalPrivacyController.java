package com.tanalytics.privacy.controller;

import com.tanalytics.privacy.dto.InternalSaltResponse;
import com.tanalytics.privacy.model.DailySalt;
import com.tanalytics.privacy.service.SaltService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/privacy")
@Tag(name = "Internal Privacy", description = "Internal service endpoints for privacy workflows")
public class InternalPrivacyController {

    private final SaltService saltService;

    public InternalPrivacyController(SaltService saltService) {
        this.saltService = saltService;
    }

    @GetMapping("/sites/{siteId}/salt")
    @Operation(summary = "Get current anonymization salt for a site")
    public ResponseEntity<InternalSaltResponse> currentSalt(@PathVariable UUID siteId) {
        DailySalt salt = saltService.getOrCreateSaltForToday(siteId);
        return ResponseEntity.ok(new InternalSaltResponse(siteId, salt.getSaltDate(), salt.getSaltValue()));
    }
}
