package com.example.verification.model.entity;

import com.example.verification.model.enums.VerifyResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "verify_record")
public class VerifyRecord extends BaseEntity {

    @Column(name = "verify_no", nullable = false, unique = true, length = 64)
    private String verifyNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sign_record_id")
    private SignRecord signRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verify_link_id")
    private VerifyLink verifyLink;

    @Column(name = "verify_mode", nullable = false, length = 32)
    private String verifyMode;

    @Column(name = "operator_ip", length = 64)
    private String operatorIp;

    @Column(name = "upload_file_name", length = 255)
    private String uploadFileName;

    @Column(name = "upload_file_size")
    private Long uploadFileSize;

    @Column(name = "upload_file_digest", length = 255)
    private String uploadFileDigest;

    @Enumerated(EnumType.STRING)
    @Column(name = "verify_result", nullable = false, length = 32)
    private VerifyResult verifyResult;

    @Column(name = "result_message", length = 255)
    private String resultMessage;

    @Column(name = "verify_time", nullable = false)
    private LocalDateTime verifyTime;

    @Column(name = "signer_name", length = 64)
    private String signerName;

    @Column(name = "original_sign_time")
    private LocalDateTime originalSignTime;

    @Column(name = "public_key", length = 255)
    private String publicKey;
}
