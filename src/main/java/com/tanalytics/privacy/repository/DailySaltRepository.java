package com.tanalytics.privacy.repository;

import com.tanalytics.privacy.model.DailySalt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailySaltRepository extends JpaRepository<DailySalt, UUID> {

    Optional<DailySalt> findBySiteIdAndSaltDate(UUID siteId, LocalDate saltDate);

    Optional<DailySalt> findTopBySiteIdOrderBySaltDateDesc(UUID siteId);

    List<DailySalt> findBySaltDate(LocalDate saltDate);
}
