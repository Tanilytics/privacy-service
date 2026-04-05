package com.tanalytics.privacy.repository;

import com.tanalytics.privacy.model.DeletionRequest;
import com.tanalytics.privacy.model.DeletionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, UUID> {

    List<DeletionRequest> findBySiteIdOrderByRequestedAtDesc(UUID siteId, Pageable pageable);

    List<DeletionRequest> findBySiteIdAndStatusOrderByRequestedAtDesc(UUID siteId, DeletionStatus status, Pageable pageable);

    List<DeletionRequest> findByStatusOrderByRequestedAtAsc(DeletionStatus status, Pageable pageable);
}
