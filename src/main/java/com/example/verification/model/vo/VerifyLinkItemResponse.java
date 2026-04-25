package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VerifyLinkItemResponse {

    private String signNo;
    private String verifyToken;
    private String verifyUrl;
    private String status;
    private Integer currentAccessCount;
    private Integer maxAccessCount;
    private LocalDateTime expiresAt;
    private LocalDateTime lastAccessAt;
}
