package com.tanalytics.privacy.service;

import com.tanalytics.privacy.dto.DeletionRequestResponse;
import com.tanalytics.privacy.model.DeletionRequest;
import com.tanalytics.privacy.model.DeletionStatus;
import com.tanalytics.privacy.repository.DeletionRequestRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeletionService {

    private static final Logger log = LoggerFactory.getLogger(DeletionService.class);

    private final DeletionRequestRepository deletionRequestRepository;
    private final ClickHouseDeletionService clickHouseDeletionService;
    private final RedisCleanupService redisCleanupService;
    private final Counter deletionRequestedCounter;
    private final Counter deletionCompletedCounter;
    private final Counter deletionFailedCounter;

    public DeletionService(
            DeletionRequestRepository deletionRequestRepository,
            ClickHouseDeletionService clickHouseDeletionService,
            RedisCleanupService redisCleanupService,
            MeterRegistry meterRegistry
    ) {
        this.deletionRequestRepository = deletionRequestRepository;
        this.clickHouseDeletionService = clickHouseDeletionService;
        this.redisCleanupService = redisCleanupService;
        this.deletionRequestedCounter = meterRegistry.counter("privacy.deletions.requested.total");
        this.deletionCompletedCounter = meterRegistry.counter("privacy.deletions.completed.total");
        this.deletionFailedCounter = meterRegistry.counter("privacy.deletions.failed.total");
    }

    @Transactional
    public DeletionRequestResponse requestDeletion(UUID siteId, String visitorId) {
        DeletionRequest request = new DeletionRequest();
        request.setSiteId(siteId);
        request.setVisitorId(visitorId);
        request.setRequestedAt(Instant.now());
        request.setStatus(DeletionStatus.PENDING);

        DeletionRequest saved = deletionRequestRepository.save(request);
        deletionRequestedCounter.increment();
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DeletionRequestResponse> listDeletions(UUID siteId, DeletionStatus status, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        Pageable pageable = PageRequest.of(0, safeLimit);

        List<DeletionRequest> requests = status == null
                ? deletionRequestRepository.findBySiteIdOrderByRequestedAtDesc(siteId, pageable)
                : deletionRequestRepository.findBySiteIdAndStatusOrderByRequestedAtDesc(siteId, status, pageable);

        return requests.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void processPendingDeletions(int batchSize) {
        List<DeletionRequest> pending = deletionRequestRepository.findByStatusOrderByRequestedAtAsc(
                DeletionStatus.PENDING,
                PageRequest.of(0, Math.max(batchSize, 1))
        );

        for (DeletionRequest request : pending) {
            request.setStatus(DeletionStatus.PROCESSING);
            request.setErrorMessage(null);
            deletionRequestRepository.save(request);

            try {
                redisCleanupService.cleanupVisitorData(request.getSiteId(), request.getVisitorId());
                clickHouseDeletionService.requestVisitorDeletion(request.getSiteId(), request.getVisitorId());
                boolean deleted = clickHouseDeletionService.verifyVisitorDeletion(request.getSiteId(), request.getVisitorId());

                if (deleted) {
                    request.setStatus(DeletionStatus.COMPLETED);
                    request.setCompletedAt(Instant.now());
                    request.setErrorMessage(null);
                    deletionCompletedCounter.increment();
                } else {
                    request.setStatus(DeletionStatus.FAILED);
                    request.setErrorMessage("Deletion mutation accepted but verification still found rows");
                    deletionFailedCounter.increment();
                }
            } catch (Exception ex) {
                log.error("Deletion workflow failed for request {}", request.getId(), ex);
                request.setStatus(DeletionStatus.FAILED);
                request.setErrorMessage(ex.getMessage());
                deletionFailedCounter.increment();
            }

            deletionRequestRepository.save(request);
        }
    }

    private DeletionRequestResponse toResponse(DeletionRequest request) {
        return new DeletionRequestResponse(
                request.getId(),
                request.getSiteId(),
                request.getVisitorId(),
                request.getStatus(),
                request.getErrorMessage(),
                request.getRequestedAt(),
                request.getCompletedAt()
        );
    }
}
