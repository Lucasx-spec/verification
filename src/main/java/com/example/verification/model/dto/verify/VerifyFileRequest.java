package com.example.verification.model.dto.verify;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyFileRequest {

    @NotBlank(message = "验证令牌不能为空")
    private String verifyToken;

    @NotBlank(message = "上传文件摘要不能为空")
    private String uploadFileDigest;

    private String uploadFileName;
    private Long uploadFileSize;
}
