package com.tanalytics.privacy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_salts")
public class DailySalt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "salt_date", nullable = false)
    private LocalDate saltDate;

    @Column(name = "salt_value", nullable = false)
    private String saltValue;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DailySalt() {}

    public UUID getId() {
        return id;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public LocalDate getSaltDate() {
        return saltDate;
    }

    public void setSaltDate(LocalDate saltDate) {
        this.saltDate = saltDate;
    }

    public String getSaltValue() {
        return saltValue;
    }

    public void setSaltValue(String saltValue) {
        this.saltValue = saltValue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
