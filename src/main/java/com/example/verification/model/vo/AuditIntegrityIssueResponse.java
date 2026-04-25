package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditIntegrityIssueResponse {

    private Long chainIndex;
    private String logNo;
    private String issueType;
    private String message;
}
