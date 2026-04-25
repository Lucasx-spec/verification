package com.example.verification.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    @NotBlank(message = "签名公钥不能为空")
    private String publicKey;

    private String publicKeyFormat;
    private String realName;
    private String nickname;
    private String email;
    private String phone;
}
