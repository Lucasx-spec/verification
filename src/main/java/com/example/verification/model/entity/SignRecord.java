package com.example.verification.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "sign_record")
public class SignRecord extends BaseEntity {

    @Column(name = "sign_no", nullable = false, unique = true, length = 64)
    private String signNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", length = 64)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 128)
    private String mimeType;

    @Column(name = "digest_algorithm", nullable = false, length = 32)
    private String digestAlgorithm;

    @Column(name = "file_digest", nullable = false, length = 255)
    private String fileDigest;

    @Column(name = "signature_algorithm", nullable = false, length = 32)
    private String signatureAlgorithm;

    @Column(name = "signature_value", nullable = false, columnDefinition = "TEXT")
    private String signatureValue;

    @Column(name = "signature_format", nullable = false, length = 64)
    private String signatureFormat;

    @Column(name = "sign_time", nullable = false)
    private LocalDateTime signTime;

    @Column(name = "signer_name", nullable = false, length = 64)
    private String signerName;

    @Column(name = "signer_account", nullable = false, length = 64)
    private String signerAccount;

    @Column(name = "public_key", nullable = false, length = 255)
    private String publicKey;

    @Column(name = "public_key_format", nullable = false, length = 64)
    private String publicKeyFormat;

    @Column(name = "sign_remark", length = 255)
    private String signRemark;
}
