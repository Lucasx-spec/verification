package com.example.verification.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SignRecordItemResponse {

    private String signNo;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileDigest;
    private String signatureFormat;
    private LocalDateTime signTime;
    private String signerName;
    private String signRemark;
}
