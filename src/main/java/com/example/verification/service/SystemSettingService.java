package com.example.verification.service;

import com.example.verification.model.dto.system.UpdateSystemSettingsRequest;
import com.example.verification.model.vo.SystemSettingsResponse;

public interface SystemSettingService {

    SystemSettingsResponse getSystemSettings();

    SystemSettingsResponse updateSystemSettings(Long userId, String username, UpdateSystemSettingsRequest request);

    int getSm3ChunkSizeKb();

    long getVerifyLinkExpireHours();

    int getVerifyLinkMaxAccessCount();

    int getUploadMaxFileSizeMb();
}
