package com.tanalytics.privacy.service;

import com.tanalytics.privacy.auth.InternalAuthClient;
import com.tanalytics.privacy.auth.InternalSiteConfigResponse;
import com.tanalytics.privacy.model.DailySalt;
import com.tanalytics.privacy.repository.DailySaltRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class SaltService {

    private static final int SALT_BYTES = 32;

    private final DailySaltRepository dailySaltRepository;
    private final InternalAuthClient internalAuthClient;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Counter rotationCounter;

    public SaltService(
            DailySaltRepository dailySaltRepository,
            InternalAuthClient internalAuthClient,
            MeterRegistry meterRegistry
    ) {
        this.dailySaltRepository = dailySaltRepository;
        this.internalAuthClient = internalAuthClient;
        this.rotationCounter = meterRegistry.counter("privacy.salt.rotation.total");
    }

    @Transactional
    public DailySalt getOrCreateSaltForToday(UUID siteId) {
        LocalDate today = LocalDate.now();
        return dailySaltRepository.findBySiteIdAndSaltDate(siteId, today)
                .orElseGet(() -> createSalt(siteId, today));
    }

    @Transactional
    public void rotateForAllSites() {
        LocalDate today = LocalDate.now();
        List<InternalSiteConfigResponse> sites = internalAuthClient.getAllSiteConfigs();
        for (InternalSiteConfigResponse site : sites) {
            dailySaltRepository.findBySiteIdAndSaltDate(site.siteId(), today)
                    .orElseGet(() -> createSalt(site.siteId(), today));
        }
    }

    private DailySalt createSalt(UUID siteId, LocalDate date) {
        byte[] bytes = new byte[SALT_BYTES];
        secureRandom.nextBytes(bytes);

        DailySalt salt = new DailySalt();
        salt.setSiteId(siteId);
        salt.setSaltDate(date);
        salt.setSaltValue(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        salt.setCreatedAt(Instant.now());
        rotationCounter.increment();
        return dailySaltRepository.save(salt);
    }
}
