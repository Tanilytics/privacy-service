package com.tanalytics.privacy.service;

import com.tanalytics.privacy.dto.ExportResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExportService {

    private final JdbcTemplate clickHouseJdbcTemplate;

    public ExportService(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }

    public ExportResponse exportVisitorData(UUID siteId, String visitorId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 5000);
        String sql = """
                SELECT event_id, event_type, timestamp, url, referrer, properties, media_url, media_type, media_duration, media_position
                FROM events
                WHERE site_id = ? AND visitor_id = ?
                ORDER BY timestamp ASC
                LIMIT ?
                """;

        List<Map<String, Object>> events = clickHouseJdbcTemplate.queryForList(sql, siteId.toString(), visitorId, safeLimit);
        return new ExportResponse(siteId, visitorId, Instant.now(), events.size(), events);
    }
}
