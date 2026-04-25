package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyLinkResponse {

    private String signNo;
    private String verifyToken;
    private String verifyUrl;
}
