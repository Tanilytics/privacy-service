package com.tanalytics.privacy.repository;

import com.tanalytics.privacy.model.ConsentRecord;
import com.tanalytics.privacy.model.ConsentScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {

    @Query("""
            SELECT c.consentScope, COUNT(c)
            FROM ConsentRecord c
            WHERE c.siteId = :siteId
              AND c.recordedAt BETWEEN :from AND :to
            GROUP BY c.consentScope
            """)
    List<Object[]> countByScope(
            @Param("siteId") UUID siteId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    long countBySiteIdAndRecordedAtBetween(UUID siteId, Instant from, Instant to);

    long countBySiteIdAndConsentGivenAndRecordedAtBetween(UUID siteId, boolean consentGiven, Instant from, Instant to);

    long countBySiteIdAndConsentScopeAndRecordedAtBetween(UUID siteId, ConsentScope consentScope, Instant from, Instant to);
}
