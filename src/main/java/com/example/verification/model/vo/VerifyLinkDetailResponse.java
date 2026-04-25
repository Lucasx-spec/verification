package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VerifyLinkDetailResponse {

    private String signNo;
    private String fileName;
    private String signerName;
    private String verifyToken;
    private String verifyUrl;
    private String status;
    private String randomFactor;
    private String timeFactor;
    private Integer currentAccessCount;
    private Integer maxAccessCount;
    private LocalDateTime expiresAt;
    private LocalDateTime lastAccessAt;
    private LocalDateTime createdAt;
}
