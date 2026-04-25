package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SignRecordDetailResponse {

    private String signNo;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String mimeType;
    private String digestAlgorithm;
    private String fileDigest;
    private String signatureAlgorithm;
    private String signatureValue;
    private String signatureFormat;
    private LocalDateTime signTime;
    private String signerName;
    private String signerAccount;
    private String publicKey;
    private String publicKeyFormat;
    private String signRemark;
}
