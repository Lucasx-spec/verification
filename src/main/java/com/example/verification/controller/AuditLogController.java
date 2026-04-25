package com.example.verification.controller;

import com.example.verification.common.ApiResponse;
import com.example.verification.model.vo.AuditIntegrityCheckResponse;
import com.example.verification.model.vo.AuditLogItemResponse;
import com.example.verification.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/logs")
    public ApiResponse<List<AuditLogItemResponse>> listMyAuditLogs(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return ApiResponse.success(auditLogService.listUserLogs(userId));
    }

    @GetMapping("/admin/logs")
    public ApiResponse<List<AuditLogItemResponse>> listAllAuditLogs() {
        return ApiResponse.success(auditLogService.listAllLogs());
    }

    @GetMapping("/integrity")
    public ApiResponse<AuditIntegrityCheckResponse> checkIntegrity() {
        return ApiResponse.success("日志完整性校验完成", auditLogService.checkIntegrity());
    }
}
