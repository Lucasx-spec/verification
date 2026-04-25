package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyResultResponse {

    private String signNo;
    private String signerName;
    private String verifyResult;
    private String resultMessage;
    private String digestAlgorithm;
    private String fileDigest;
    private String signatureAlgorithm;
}
