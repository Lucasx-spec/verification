package com.example.verification.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateKeyPairRequest {

    @NotBlank(message = "签名公钥不能为空")
    private String publicKey;

    @NotBlank(message = "公钥格式不能为空")
    private String publicKeyFormat;
}
