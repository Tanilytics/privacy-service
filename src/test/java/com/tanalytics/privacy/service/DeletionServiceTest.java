package com.tanalytics.privacy.service;

import com.tanalytics.privacy.dto.DeletionRequestResponse;
import com.tanalytics.privacy.model.DeletionRequest;
import com.tanalytics.privacy.model.DeletionStatus;
import com.tanalytics.privacy.repository.DeletionRequestRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletionServiceTest {

    @Mock
    private DeletionRequestRepository deletionRequestRepository;

    @Mock
    private ClickHouseDeletionService clickHouseDeletionService;

    @Mock
    private RedisCleanupService redisCleanupService;

    @Test
    void requestDeletionCreatesPendingRecord() {
        DeletionService service = new DeletionService(
                deletionRequestRepository,
                clickHouseDeletionService,
                redisCleanupService,
                new SimpleMeterRegistry()
        );

        UUID siteId = UUID.randomUUID();
        DeletionRequest saved = new DeletionRequest();
        saved.setSiteId(siteId);
        saved.setVisitorId("visitor-1");
        saved.setStatus(DeletionStatus.PENDING);
        saved.setRequestedAt(Instant.now());

        when(deletionRequestRepository.save(any(DeletionRequest.class))).thenReturn(saved);

        DeletionRequestResponse response = service.requestDeletion(siteId, "visitor-1");

        assertThat(response.siteId()).isEqualTo(siteId);
        assertThat(response.visitorId()).isEqualTo("visitor-1");
        assertThat(response.status()).isEqualTo(DeletionStatus.PENDING);
    }
}
