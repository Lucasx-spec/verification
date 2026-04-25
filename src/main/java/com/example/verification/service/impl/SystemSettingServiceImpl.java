package com.example.verification.service.impl;

import com.example.verification.model.dto.system.UpdateSystemSettingsRequest;
import com.example.verification.model.entity.SystemSetting;
import com.example.verification.model.enums.OperationResult;
import com.example.verification.model.vo.SystemSettingsResponse;
import com.example.verification.repository.SystemSettingRepository;
import com.example.verification.service.AuditLogService;
import com.example.verification.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private static final String SM3_CHUNK_SIZE_KB = "sm3.chunk.size.kb";
    private static final String VERIFY_LINK_EXPIRE_HOURS = "verify.link.expire.hours";
    private static final String VERIFY_LINK_MAX_ACCESS_COUNT = "verify.link.max.access.count";
    private static final String UPLOAD_MAX_FILE_SIZE_MB = "upload.max.file.size.mb";

    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogService auditLogService;

    @Value("${verification.security.verify-link-expire-hours:24}")
    private long defaultVerifyLinkExpireHours;

    @Value("${verification.security.verify-link-max-access-count:20}")
    private int defaultVerifyLinkMaxAccessCount;

    @Value("${verification.crypto.sm3-chunk-size-kb:1024}")
    private int defaultSm3ChunkSizeKb;

    @Value("${verification.upload.max-file-size-mb:50}")
    private int defaultUploadMaxFileSizeMb;

    @Override
    @Transactional(readOnly = true)
    public SystemSettingsResponse getSystemSettings() {
        Map<String, SystemSetting> settings = loadSettings();
        return SystemSettingsResponse.builder()
                .sm3ChunkSizeKb(parseInt(settings.get(SM3_CHUNK_SIZE_KB), defaultSm3ChunkSizeKb))
                .verifyLinkExpireHours(parseInt(settings.get(VERIFY_LINK_EXPIRE_HOURS), (int) defaultVerifyLinkExpireHours))
                .verifyLinkMaxAccessCount(parseInt(settings.get(VERIFY_LINK_MAX_ACCESS_COUNT), defaultVerifyLinkMaxAccessCount))
                .uploadMaxFileSizeMb(parseInt(settings.get(UPLOAD_MAX_FILE_SIZE_MB), defaultUploadMaxFileSizeMb))
                .build();
    }

    @Override
    @Transactional
    public SystemSettingsResponse updateSystemSettings(Long userId, String username, UpdateSystemSettingsRequest request) {
        LocalDateTime now = LocalDateTime.now();
        saveOrUpdate(SM3_CHUNK_SIZE_KB, String.valueOf(request.getSm3ChunkSizeKb()), "SM3 分块大小", "前端计算 SM3 摘要时的单块大小，单位 KB", now);
        saveOrUpdate(VERIFY_LINK_EXPIRE_HOURS, String.valueOf(request.getVerifyLinkExpireHours()), "验签链接有效期", "公开验签链接默认有效期，单位小时", now);
        saveOrUpdate(VERIFY_LINK_MAX_ACCESS_COUNT, String.valueOf(request.getVerifyLinkMaxAccessCount()), "单链接最大访问次数", "单个公开验签链接允许的最大访问次数", now);
        saveOrUpdate(UPLOAD_MAX_FILE_SIZE_MB, String.valueOf(request.getUploadMaxFileSizeMb()), "文件上传大小限制", "前后端上传文件允许的最大大小，单位 MB", now);
        auditLogService.saveLog("SYSTEM", "UPDATE_SYSTEM_SETTINGS", "更新系统参数", OperationResult.SUCCESS, "系统参数更新成功", userId, username);
        return getSystemSettings();
    }

    @Override
    @Transactional(readOnly = true)
    public int getSm3ChunkSizeKb() {
        return getSystemSettings().getSm3ChunkSizeKb();
    }

    @Override
    @Transactional(readOnly = true)
    public long getVerifyLinkExpireHours() {
        return getSystemSettings().getVerifyLinkExpireHours();
    }

    @Override
    @Transactional(readOnly = true)
    public int getVerifyLinkMaxAccessCount() {
        return getSystemSettings().getVerifyLinkMaxAccessCount();
    }

    @Override
    @Transactional(readOnly = true)
    public int getUploadMaxFileSizeMb() {
        return getSystemSettings().getUploadMaxFileSizeMb();
    }

    private Map<String, SystemSetting> loadSettings() {
        List<SystemSetting> items = systemSettingRepository.findAll();
        Map<String, SystemSetting> settings = new LinkedHashMap<>();
        for (SystemSetting item : items) {
            settings.put(item.getSettingKey(), item);
        }
        return settings;
    }

    private int parseInt(SystemSetting setting, int defaultValue) {
        if (setting == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(setting.getSettingValue());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void saveOrUpdate(String key, String value, String name, String desc, LocalDateTime now) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key).orElseGet(SystemSetting::new);
        if (setting.getCreatedAt() == null) {
            setting.setCreatedAt(now);
            setting.setSettingKey(key);
        }
        setting.setSettingName(name);
        setting.setSettingDesc(desc);
        setting.setSettingValue(value);
        setting.setUpdatedAt(now);
        systemSettingRepository.save(setting);
    }
}
