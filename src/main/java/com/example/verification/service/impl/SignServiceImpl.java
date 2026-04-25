package com.example.verification.service.impl;

import com.example.verification.common.ApiCode;
import com.example.verification.common.BusinessException;
import com.example.verification.model.dto.sign.CreateSignRecordRequest;
import com.example.verification.model.entity.SignRecord;
import com.example.verification.model.entity.User;
import com.example.verification.model.enums.OperationResult;
import com.example.verification.model.vo.SignRecordDetailResponse;
import com.example.verification.model.vo.SignRecordItemResponse;
import com.example.verification.repository.SignRecordRepository;
import com.example.verification.service.AuditLogService;
import com.example.verification.service.AuthService;
import com.example.verification.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {

    private final SignRecordRepository signRecordRepository;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public SignRecordDetailResponse createSignRecord(Long userId, CreateSignRecordRequest request) {
        User user = authService.getRequiredUser(userId);

        SignRecord signRecord = new SignRecord();
        signRecord.setSignNo("SIGN-" + UUID.randomUUID().toString().replace("-", ""));
        signRecord.setUser(user);
        signRecord.setFileName(request.getFileName());
        signRecord.setFileType(request.getFileType());
        signRecord.setFileSize(request.getFileSize());
        signRecord.setMimeType(request.getMimeType());
        signRecord.setDigestAlgorithm("SM3");
        signRecord.setFileDigest(request.getFileDigest());
        signRecord.setSignatureAlgorithm("SM2");
        signRecord.setSignatureValue(request.getSignatureValue());
        signRecord.setSignatureFormat(request.getSignatureFormat());
        signRecord.setSignTime(LocalDateTime.now());
        signRecord.setSignerName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        signRecord.setSignerAccount(user.getUsername());
        signRecord.setPublicKey(user.getPublicKey());
        signRecord.setPublicKeyFormat(user.getPublicKeyFormat());
        signRecord.setSignRemark(request.getSignRemark());
        signRecord.setCreatedAt(LocalDateTime.now());
        signRecord.setUpdatedAt(LocalDateTime.now());
        SignRecord saved = signRecordRepository.save(signRecord);
        auditLogService.saveLog("SIGN", "CREATE_SIGN_RECORD", "创建签名记录", OperationResult.SUCCESS, "签名记录创建成功", user.getId(), user.getUsername());
        return toDetailResponse(saved);
    }

    @Override
    public SignRecordDetailResponse getBySignNo(String signNo) {
        SignRecord signRecord = signRecordRepository.findBySignNo(signNo)
                .orElseThrow(() -> new BusinessException(ApiCode.SIGN_RECORD_NOT_FOUND, "签名记录不存在"));
        return toDetailResponse(signRecord);
    }

    @Override
    public List<SignRecordItemResponse> listUserSignRecords(Long userId) {
        return signRecordRepository.findByUserIdOrderBySignTimeDesc(userId)
                .stream()
                .map(record -> SignRecordItemResponse.builder()
                        .signNo(record.getSignNo())
                        .fileName(record.getFileName())
                        .fileType(record.getFileType())
                        .fileSize(record.getFileSize())
                        .fileDigest(record.getFileDigest())
                        .signatureFormat(record.getSignatureFormat())
                        .signTime(record.getSignTime())
                        .signerName(record.getSignerName())
                        .signRemark(record.getSignRemark())
                        .build())
                .toList();
    }

    @Override
    public List<SignRecordItemResponse> listAllSignRecords() {
        return signRecordRepository.findAllByOrderBySignTimeDesc()
                .stream()
                .map(record -> SignRecordItemResponse.builder()
                        .signNo(record.getSignNo())
                        .fileName(record.getFileName())
                        .fileType(record.getFileType())
                        .fileSize(record.getFileSize())
                        .fileDigest(record.getFileDigest())
                        .signatureFormat(record.getSignatureFormat())
                        .signTime(record.getSignTime())
                        .signerName(record.getSignerName())
                        .signRemark(record.getSignRemark())
                        .build())
                .toList();
    }

    private SignRecordDetailResponse toDetailResponse(SignRecord record) {
        return SignRecordDetailResponse.builder()
                .signNo(record.getSignNo())
                .fileName(record.getFileName())
                .fileType(record.getFileType())
                .fileSize(record.getFileSize())
                .mimeType(record.getMimeType())
                .digestAlgorithm(record.getDigestAlgorithm())
                .fileDigest(record.getFileDigest())
                .signatureAlgorithm(record.getSignatureAlgorithm())
                .signatureValue(record.getSignatureValue())
                .signatureFormat(record.getSignatureFormat())
                .signTime(record.getSignTime())
                .signerName(record.getSignerName())
                .signerAccount(record.getSignerAccount())
                .publicKey(record.getPublicKey())
                .publicKeyFormat(record.getPublicKeyFormat())
                .signRemark(record.getSignRemark())
                .build();
    }
}
