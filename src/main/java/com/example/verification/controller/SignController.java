package com.example.verification.controller;

import com.example.verification.common.ApiResponse;
import com.example.verification.model.dto.sign.CreateSignRecordRequest;
import com.example.verification.model.vo.SignRecordDetailResponse;
import com.example.verification.model.vo.SignRecordItemResponse;
import com.example.verification.service.SignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sign")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    @PostMapping("/records")
    public ApiResponse<SignRecordDetailResponse> createSignRecord(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                                  @Valid @RequestBody CreateSignRecordRequest request) {
        return ApiResponse.success("签名记录创建成功", signService.createSignRecord(userId, request));
    }

    @GetMapping("/records/{signNo}")
    public ApiResponse<SignRecordDetailResponse> getSignRecord(@PathVariable String signNo) {
        return ApiResponse.success(signService.getBySignNo(signNo));
    }

    @GetMapping("/records")
    public ApiResponse<List<SignRecordItemResponse>> listMySignRecords(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return ApiResponse.success(signService.listUserSignRecords(userId));
    }

    @GetMapping("/admin/records")
    public ApiResponse<List<SignRecordItemResponse>> listAllSignRecords() {
        return ApiResponse.success(signService.listAllSignRecords());
    }
}
