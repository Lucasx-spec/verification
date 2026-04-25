package com.example.verification.controller;

import com.example.verification.common.ApiResponse;
import com.example.verification.model.dto.auth.LoginRequest;
import com.example.verification.model.dto.auth.RegisterRequest;
import com.example.verification.model.dto.auth.UpdateKeyPairRequest;
import com.example.verification.model.vo.AuthResponse;
import com.example.verification.model.vo.CurrentUserResponse;
import com.example.verification.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.success("登录成功", authService.login(request, httpServletRequest.getRemoteAddr()));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> currentUser(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return ApiResponse.success(authService.getCurrentUser(userId));
    }

    @PutMapping("/keypair")
    public ApiResponse<CurrentUserResponse> updateCurrentUserKeyPair(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                                     @Valid @RequestBody UpdateKeyPairRequest request) {
        return ApiResponse.success("密钥对已更新", authService.updateCurrentUserKeyPair(userId, request));
    }
}
