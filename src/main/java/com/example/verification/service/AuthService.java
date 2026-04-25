package com.example.verification.service;

import com.example.verification.model.dto.auth.LoginRequest;
import com.example.verification.model.dto.auth.RegisterRequest;
import com.example.verification.model.dto.auth.UpdateKeyPairRequest;
import com.example.verification.model.entity.User;
import com.example.verification.model.vo.AuthResponse;
import com.example.verification.model.vo.CurrentUserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String clientIp);

    CurrentUserResponse getCurrentUser(Long userId);

    CurrentUserResponse updateCurrentUserKeyPair(Long userId, UpdateKeyPairRequest request);

    User getRequiredUser(Long userId);
}
