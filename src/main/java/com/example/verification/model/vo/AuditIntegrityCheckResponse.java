package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuditIntegrityCheckResponse {

    private Boolean valid;
    private Integer totalLogs;
    private Integer issueCount;
    private String latestHash;
    private List<AuditIntegrityIssueResponse> issues;
}
