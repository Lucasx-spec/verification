package com.example.verification.model.entity;

import com.example.verification.model.enums.UserStatus;
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
@Table(name = "sys_user")
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "nickname", length = 64)
    private String nickname;

    @Column(name = "real_name", length = 64)
    private String realName;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "password_salt", nullable = false, length = 255)
    private String passwordSalt;

    @Column(name = "password_updated_at")
    private LocalDateTime passwordUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserStatus status;

    @Column(name = "role_code", nullable = false, length = 32)
    private String roleCode;

    @Column(name = "login_fail_count", nullable = false)
    private Integer loginFailCount;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    @Column(name = "public_key", nullable = false, length = 255)
    private String publicKey;

    @Column(name = "public_key_format", nullable = false, length = 64)
    private String publicKeyFormat;

    @Column(name = "sign_enabled", nullable = false)
    private Boolean signEnabled;

    @Column(name = "sign_initialized_at")
    private LocalDateTime signInitializedAt;
}
