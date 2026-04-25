package com.example.verification.service.impl;

import com.example.verification.common.ApiCode;
import com.example.verification.common.BusinessException;
import com.example.verification.model.dto.auth.LoginRequest;
import com.example.verification.model.dto.auth.RegisterRequest;
import com.example.verification.model.dto.auth.UpdateKeyPairRequest;
import com.example.verification.model.entity.User;
import com.example.verification.model.enums.OperationResult;
import com.example.verification.model.enums.UserStatus;
import com.example.verification.model.vo.AuthResponse;
import com.example.verification.model.vo.CurrentUserResponse;
import com.example.verification.repository.UserRepository;
import com.example.verification.security.JwtTokenProvider;
import com.example.verification.service.AuditLogService;
import com.example.verification.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ApiCode.USER_ALREADY_EXISTS, "用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPasswordSalt(UUID.randomUUID().toString().replace("-", ""));
        user.setPasswordUpdatedAt(now);
        user.setStatus(UserStatus.ACTIVE);
        user.setRoleCode("USER");
        user.setLoginFailCount(0);
        user.setPublicKey(request.getPublicKey());
        user.setPublicKeyFormat(request.getPublicKeyFormat() == null ? "HEX_UNCOMPRESSED" : request.getPublicKeyFormat());
        user.setSignEnabled(true);
        user.setSignInitializedAt(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        auditLogService.saveLog("AUTH", "REGISTER", "用户注册", OperationResult.SUCCESS, "注册成功", user.getId(), user.getUsername());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ApiCode.INVALID_CREDENTIALS, "用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.setLoginFailCount(user.getLoginFailCount() + 1);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            auditLogService.saveLog("AUTH", "LOGIN", "用户登录", OperationResult.FAILURE, "用户名或密码错误", user.getId(), user.getUsername());
            throw new BusinessException(ApiCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }

        user.setLoginFailCount(0);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        auditLogService.saveLog("AUTH", "LOGIN", "用户登录", OperationResult.SUCCESS, "登录成功", user.getId(), user.getUsername());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .token(token)
                .build();
    }

    @Override
    public CurrentUserResponse getCurrentUser(Long userId) {
        User user = getRequiredUser(userId);
        return toCurrentUserResponse(user);
    }

    @Override
    @Transactional
    public CurrentUserResponse updateCurrentUserKeyPair(Long userId, UpdateKeyPairRequest request) {
        User user = getRequiredUser(userId);
        LocalDateTime now = LocalDateTime.now();
        user.setPublicKey(request.getPublicKey());
        user.setPublicKeyFormat(request.getPublicKeyFormat());
        user.setSignInitializedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);
        auditLogService.saveLog("AUTH", "RESET_KEYPAIR", "重置账户密钥对", OperationResult.SUCCESS, "账户已绑定新的公钥", user.getId(), user.getUsername());
        return toCurrentUserResponse(user);
    }

    @Override
    public User getRequiredUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ApiCode.USER_NOT_FOUND, "用户不存在"));
    }

    private CurrentUserResponse toCurrentUserResponse(User user) {
        return CurrentUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleCode(user.getRoleCode())
                .signEnabled(user.getSignEnabled())
                .publicKey(user.getPublicKey())
                .build();
    }
}
