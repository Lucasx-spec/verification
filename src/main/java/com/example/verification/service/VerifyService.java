package com.example.verification.service;

import com.example.verification.model.dto.verify.CreateVerifyLinkRequest;
import com.example.verification.model.vo.VerifyLinkDetailResponse;
import com.example.verification.model.vo.VerifyLinkItemResponse;
import com.example.verification.model.vo.VerifyLinkResponse;
import com.example.verification.model.vo.VerifyRecordItemResponse;
import com.example.verification.model.vo.VerifyResultResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VerifyService {

    VerifyLinkResponse createVerifyLink(Long userId, CreateVerifyLinkRequest request);

    VerifyResultResponse verifyFile(String verifyToken, MultipartFile file, String clientIp);

    List<VerifyLinkItemResponse> listUserVerifyLinks(Long userId);

    List<VerifyRecordItemResponse> listUserVerifyRecords(Long userId);

    List<VerifyRecordItemResponse> listAllVerifyRecords();

    VerifyLinkDetailResponse getVerifyLinkDetail(Long userId, String verifyToken);

    void disableVerifyLink(Long userId, String verifyToken);
}
