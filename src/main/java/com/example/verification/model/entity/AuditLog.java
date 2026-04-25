package com.example.verification.model.entity;

import com.example.verification.model.enums.OperationResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Column(name = "log_no", nullable = false, unique = true, length = 64)
    private String logNo;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "module", nullable = false, length = 64)
    private String module;

    @Column(name = "operation_type", nullable = false, length = 64)
    private String operationType;

    @Column(name = "operation_desc", length = 255)
    private String operationDesc;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_result", nullable = false, length = 32)
    private OperationResult operationResult;

    @Column(name = "result_message", length = 255)
    private String resultMessage;

    @Column(name = "log_content_hash", length = 255)
    private String logContentHash;

    @Column(name = "prev_hash", length = 255)
    private String prevHash;

    @Column(name = "current_hash", length = 255)
    private String currentHash;

    @Column(name = "chain_index")
    private Long chainIndex;

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;
}
