package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogItemResponse {

    private String logNo;
    private String module;
    private String operationType;
    private String operationDesc;
    private String operationResult;
    private String resultMessage;
    private LocalDateTime operationTime;
    private Long chainIndex;
}
