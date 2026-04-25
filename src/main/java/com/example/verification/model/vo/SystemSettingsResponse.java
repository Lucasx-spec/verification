package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemSettingsResponse {

    private Integer sm3ChunkSizeKb;
    private Integer verifyLinkExpireHours;
    private Integer verifyLinkMaxAccessCount;
    private Integer uploadMaxFileSizeMb;
}
