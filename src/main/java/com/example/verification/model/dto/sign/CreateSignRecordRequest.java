package com.example.verification.model.dto.sign;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSignRecordRequest {

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    private String fileType;
    private Long fileSize;
    private String mimeType;

    @NotBlank(message = "文件摘要不能为空")
    private String fileDigest;

    @NotBlank(message = "签名值不能为空")
    private String signatureValue;

    @NotBlank(message = "签名格式不能为空")
    private String signatureFormat;

    private String signRemark;
}
