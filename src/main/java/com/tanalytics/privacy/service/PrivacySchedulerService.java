package com.tanalytics.privacy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PrivacySchedulerService {

    private static final Logger log = LoggerFactory.getLogger(PrivacySchedulerService.class);

    private final DeletionService deletionService;
    private final RetentionService retentionService;
    private final SaltService saltService;

    @Value("${privacy.scheduler.deletion-batch-size:100}")
    private int deletionBatchSize;

    public PrivacySchedulerService(
            DeletionService deletionService,
            RetentionService retentionService,
            SaltService saltService
    ) {
        this.deletionService = deletionService;
        this.retentionService = retentionService;
        this.saltService = saltService;
    }

    @Scheduled(cron = "${privacy.scheduler.deletion-cron:0 0 2 * * *}")
    public void processPendingDeletions() {
        log.info("Running scheduled deletion batch with size {}", deletionBatchSize);
        deletionService.processPendingDeletions(deletionBatchSize);
    }

    @Scheduled(cron = "${privacy.scheduler.retention-cron:0 30 3 * * *}")
    public void enforceRetentionPolicies() {
        log.info("Running scheduled retention enforcement");
        retentionService.enforceRetentionPolicies();
    }

    @Scheduled(cron = "${privacy.scheduler.salt-rotation-cron:0 0 0 * * *}")
    public void rotateSalts() {
        log.info("Running scheduled salt rotation");
        saltService.rotateForAllSites();
    }
}
