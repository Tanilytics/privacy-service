package com.tanalytics.privacy.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClickHouseDeletionService {

    private final JdbcTemplate clickHouseJdbcTemplate;

    public ClickHouseDeletionService(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }

    @Retryable(retryFor = DataAccessException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void requestVisitorDeletion(UUID siteId, String visitorId) {
        clickHouseJdbcTemplate.update(
                "ALTER TABLE events DELETE WHERE site_id = ? AND visitor_id = ?",
                siteId.toString(),
                visitorId
        );
    }

    @Retryable(retryFor = DataAccessException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public boolean verifyVisitorDeletion(UUID siteId, String visitorId) {
        Integer count = clickHouseJdbcTemplate.queryForObject(
                "SELECT count() FROM events WHERE site_id = ? AND visitor_id = ?",
                Integer.class,
                siteId.toString(),
                visitorId
        );
        return count != null && count == 0;
    }

    @Retryable(retryFor = DataAccessException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void enforceRetention(UUID siteId, int retentionDays) {
        int safeRetentionDays = Math.max(retentionDays, 30);
        String sql = "ALTER TABLE events DELETE WHERE site_id = ? AND timestamp < now() - INTERVAL " + safeRetentionDays + " DAY";
        clickHouseJdbcTemplate.update(sql, siteId.toString());
    }
}
