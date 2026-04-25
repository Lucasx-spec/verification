package com.example.verification.controller;

import com.example.verification.common.ApiResponse;
import com.example.verification.model.dto.verify.CreateVerifyLinkRequest;
import com.example.verification.model.vo.VerifyLinkDetailResponse;
import com.example.verification.model.vo.VerifyLinkItemResponse;
import com.example.verification.model.vo.VerifyLinkResponse;
import com.example.verification.model.vo.VerifyRecordItemResponse;
import com.example.verification.model.vo.VerifyResultResponse;
import com.example.verification.service.VerifyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/verify")
@RequiredArgsConstructor
public class VerifyController {

    private final VerifyService verifyService;

    @PostMapping("/links")
    public ApiResponse<VerifyLinkResponse> createVerifyLink(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                            @Valid @RequestBody CreateVerifyLinkRequest request) {
        return ApiResponse.success("验签链接创建成功", verifyService.createVerifyLink(userId, request));
    }

    @PostMapping("/files")
    public ApiResponse<VerifyResultResponse> verifyFile(@RequestParam("verifyToken") String verifyToken,
                                                        @RequestParam("file") MultipartFile file,
                                                        HttpServletRequest httpServletRequest) {
        return ApiResponse.success("验签完成", verifyService.verifyFile(verifyToken, file, httpServletRequest.getRemoteAddr()));
    }

    @GetMapping("/links")
    public ApiResponse<List<VerifyLinkItemResponse>> listMyVerifyLinks(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return ApiResponse.success(verifyService.listUserVerifyLinks(userId));
    }

    @GetMapping("/records")
    public ApiResponse<List<VerifyRecordItemResponse>> listMyVerifyRecords(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return ApiResponse.success(verifyService.listUserVerifyRecords(userId));
    }

    @GetMapping("/admin/records")
    public ApiResponse<List<VerifyRecordItemResponse>> listAllVerifyRecords() {
        return ApiResponse.success(verifyService.listAllVerifyRecords());
    }

    @GetMapping("/links/{verifyToken}")
    public ApiResponse<VerifyLinkDetailResponse> getVerifyLinkDetail(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                                     @PathVariable String verifyToken) {
        return ApiResponse.success(verifyService.getVerifyLinkDetail(userId, verifyToken));
    }

    @PutMapping("/links/{verifyToken}/disable")
    public ApiResponse<Void> disableVerifyLink(@AuthenticationPrincipal(expression = "userId") Long userId,
                                               @PathVariable String verifyToken) {
        verifyService.disableVerifyLink(userId, verifyToken);
        return ApiResponse.success("验签链接已禁用", null);
    }
}
