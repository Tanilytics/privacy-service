package com.tanalytics.privacy.service;

import com.tanalytics.privacy.dto.ConsentStatsResponse;
import com.tanalytics.privacy.model.ConsentRecord;
import com.tanalytics.privacy.model.ConsentScope;
import com.tanalytics.privacy.repository.ConsentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ConsentService {

    private final ConsentRecordRepository consentRecordRepository;

    public ConsentService(ConsentRecordRepository consentRecordRepository) {
        this.consentRecordRepository = consentRecordRepository;
    }

    @Transactional
    public ConsentRecord recordConsent(UUID siteId, String visitorId, boolean consentGiven, ConsentScope scope, String ipHash) {
        ConsentRecord record = new ConsentRecord();
        record.setSiteId(siteId);
        record.setVisitorId(visitorId);
        record.setConsentGiven(consentGiven);
        record.setConsentScope(scope);
        record.setIpHash(ipHash);
        record.setRecordedAt(Instant.now());
        return consentRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public ConsentStatsResponse getConsentStats(UUID siteId, Instant from, Instant to) {
        long total = consentRecordRepository.countBySiteIdAndRecordedAtBetween(siteId, from, to);
        long granted = consentRecordRepository.countBySiteIdAndConsentGivenAndRecordedAtBetween(siteId, true, from, to);
        long denied = consentRecordRepository.countBySiteIdAndConsentGivenAndRecordedAtBetween(siteId, false, from, to);

        Map<String, Long> byScope = new HashMap<>();
        byScope.put(ConsentScope.ANALYTICS.name(), consentRecordRepository.countBySiteIdAndConsentScopeAndRecordedAtBetween(siteId, ConsentScope.ANALYTICS, from, to));
        byScope.put(ConsentScope.MEDIA_TRACKING.name(), consentRecordRepository.countBySiteIdAndConsentScopeAndRecordedAtBetween(siteId, ConsentScope.MEDIA_TRACKING, from, to));

        return new ConsentStatsResponse(siteId, from, to, total, granted, denied, byScope);
    }
}
