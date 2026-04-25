package com.example.verification.model.dto.system;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSystemSettingsRequest {

    @NotNull(message = "SM3 分块大小不能为空")
    @Min(value = 256, message = "SM3 分块大小不能小于 256KB")
    @Max(value = 16384, message = "SM3 分块大小不能大于 16384KB")
    private Integer sm3ChunkSizeKb;

    @NotNull(message = "验签链接有效期不能为空")
    @Min(value = 1, message = "验签链接有效期不能小于 1 小时")
    @Max(value = 720, message = "验签链接有效期不能大于 720 小时")
    private Integer verifyLinkExpireHours;

    @NotNull(message = "单链接最大访问次数不能为空")
    @Min(value = 1, message = "单链接最大访问次数不能小于 1")
    @Max(value = 100000, message = "单链接最大访问次数不能大于 100000")
    private Integer verifyLinkMaxAccessCount;

    @NotNull(message = "文件上传大小限制不能为空")
    @Min(value = 1, message = "文件上传大小限制不能小于 1MB")
    @Max(value = 500, message = "文件上传大小限制不能大于 500MB")
    private Integer uploadMaxFileSizeMb;
}
