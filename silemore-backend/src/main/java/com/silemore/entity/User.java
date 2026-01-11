package com.silemore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import com.silemore.util.TimeUtil;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "alert_days", nullable = false)
    private Integer alertDays = 3;

    @Column(name = "reminder_time")
    private LocalTime reminderTime = LocalTime.of(20, 0);

    @Column(name = "reminder_enabled", nullable = false)
    private Boolean reminderEnabled = true;

    @Column(name = "is_paused", nullable = false)
    private Boolean isPaused = false;

    @Column(name = "paused_days_since_last_check_in", nullable = false)
    private Integer pausedDaysSinceLastCheckIn = 0;

    @Column(name = "pause_started_at")
    private LocalDateTime pauseStartedAt;

    @Column(name = "pause_until")
    private LocalDateTime pauseUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = TimeUtil.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = TimeUtil.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getAlertDays() {
        return alertDays;
    }

    public void setAlertDays(Integer alertDays) {
        this.alertDays = alertDays;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Boolean getReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(Boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public Boolean getIsPaused() {
        return isPaused;
    }

    public void setIsPaused(Boolean isPaused) {
        this.isPaused = isPaused;
    }

    public Integer getPausedDaysSinceLastCheckIn() {
        return pausedDaysSinceLastCheckIn;
    }

    public void setPausedDaysSinceLastCheckIn(Integer pausedDaysSinceLastCheckIn) {
        this.pausedDaysSinceLastCheckIn = pausedDaysSinceLastCheckIn;
    }

    public LocalDateTime getPauseUntil() {
        return pauseUntil;
    }

    public void setPauseUntil(LocalDateTime pauseUntil) {
        this.pauseUntil = pauseUntil;
    }

    public LocalDateTime getPauseStartedAt() {
        return pauseStartedAt;
    }

    public void setPauseStartedAt(LocalDateTime pauseStartedAt) {
        this.pauseStartedAt = pauseStartedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
