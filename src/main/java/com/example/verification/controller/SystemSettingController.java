package com.example.verification.controller;

import com.example.verification.common.ApiResponse;
import com.example.verification.model.dto.system.UpdateSystemSettingsRequest;
import com.example.verification.model.vo.SystemSettingsResponse;
import com.example.verification.service.SystemSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    public ApiResponse<SystemSettingsResponse> getSystemSettings() {
        return ApiResponse.success(systemSettingService.getSystemSettings());
    }

    @PutMapping
    public ApiResponse<SystemSettingsResponse> updateSystemSettings(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                                    @AuthenticationPrincipal(expression = "username") String username,
                                                                    @Valid @RequestBody UpdateSystemSettingsRequest request) {
        return ApiResponse.success("系统参数已更新", systemSettingService.updateSystemSettings(userId, username, request));
    }
}
