package com.tanalytics.privacy.service;

import com.tanalytics.privacy.auth.InternalAuthClient;
import com.tanalytics.privacy.auth.InternalSiteConfigResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetentionService {

    private static final Logger log = LoggerFactory.getLogger(RetentionService.class);

    private final InternalAuthClient internalAuthClient;
    private final ClickHouseDeletionService clickHouseDeletionService;
    private final Counter retentionRunCounter;
    private final Counter retentionFailureCounter;

    public RetentionService(
            InternalAuthClient internalAuthClient,
            ClickHouseDeletionService clickHouseDeletionService,
            MeterRegistry meterRegistry
    ) {
        this.internalAuthClient = internalAuthClient;
        this.clickHouseDeletionService = clickHouseDeletionService;
        this.retentionRunCounter = meterRegistry.counter("privacy.retention.runs.total");
        this.retentionFailureCounter = meterRegistry.counter("privacy.retention.failures.total");
    }

    public void enforceRetentionPolicies() {
        List<InternalSiteConfigResponse> siteConfigs = internalAuthClient.getAllSiteConfigs();
        for (InternalSiteConfigResponse siteConfig : siteConfigs) {
            try {
                clickHouseDeletionService.enforceRetention(siteConfig.siteId(), siteConfig.retentionDays());
                retentionRunCounter.increment();
            } catch (Exception ex) {
                retentionFailureCounter.increment();
                log.error("Retention enforcement failed for site {}", siteConfig.siteId(), ex);
            }
        }
    }
}
