package com.example.verification.model.dto.verify;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVerifyLinkRequest {

    @NotBlank(message = "签名编号不能为空")
    private String signNo;
}
