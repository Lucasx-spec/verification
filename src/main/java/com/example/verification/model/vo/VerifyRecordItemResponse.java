package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VerifyRecordItemResponse {

    private String verifyNo;
    private String signNo;
    private String verifyMode;
    private String uploadFileName;
    private String uploadFileDigest;
    private String verifyResult;
    private String resultMessage;
    private String signerName;
    private LocalDateTime verifyTime;
}
