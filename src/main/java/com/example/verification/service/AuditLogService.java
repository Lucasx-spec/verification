package com.example.verification.service;

import com.example.verification.model.entity.AuditLog;
import com.example.verification.model.enums.OperationResult;
import com.example.verification.model.vo.AuditIntegrityCheckResponse;
import com.example.verification.model.vo.AuditLogItemResponse;

import java.util.List;

public interface AuditLogService {

    AuditLog saveLog(String module, String operationType, String operationDesc, OperationResult operationResult, String resultMessage, Long userId, String username);

    List<AuditLogItemResponse> listUserLogs(Long userId);

    List<AuditLogItemResponse> listAllLogs();

    AuditIntegrityCheckResponse checkIntegrity();
}
