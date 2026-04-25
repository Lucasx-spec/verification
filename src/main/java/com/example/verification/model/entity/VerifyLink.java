package com.example.verification.model.entity;

import com.example.verification.model.enums.VerifyLinkStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "verify_link")
public class VerifyLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sign_record_id")
    private SignRecord signRecord;

    @Column(name = "verify_token", nullable = false, unique = true, length = 255)
    private String verifyToken;

    @Column(name = "random_factor", nullable = false, length = 128)
    private String randomFactor;

    @Column(name = "time_factor", nullable = false, length = 64)
    private String timeFactor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private VerifyLinkStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "current_access_count", nullable = false)
    private Integer currentAccessCount;

    @Column(name = "last_access_at")
    private LocalDateTime lastAccessAt;

    @Column(name = "last_access_ip", length = 64)
    private String lastAccessIp;
}
