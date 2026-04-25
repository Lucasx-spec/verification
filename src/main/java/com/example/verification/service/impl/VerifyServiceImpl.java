package com.example.verification.service.impl;

import com.example.verification.common.ApiCode;
import com.example.verification.common.BusinessException;
import com.example.verification.crypto.Sm2VerifyService;
import com.example.verification.crypto.Sm3DigestService;
import com.example.verification.model.dto.verify.CreateVerifyLinkRequest;
import com.example.verification.model.entity.SignRecord;
import com.example.verification.model.entity.VerifyLink;
import com.example.verification.model.entity.VerifyRecord;
import com.example.verification.model.enums.OperationResult;
import com.example.verification.model.enums.VerifyLinkStatus;
import com.example.verification.model.enums.VerifyResult;
import com.example.verification.model.vo.VerifyLinkDetailResponse;
import com.example.verification.model.vo.VerifyLinkItemResponse;
import com.example.verification.model.vo.VerifyLinkResponse;
import com.example.verification.model.vo.VerifyRecordItemResponse;
import com.example.verification.model.vo.VerifyResultResponse;
import com.example.verification.repository.SignRecordRepository;
import com.example.verification.repository.VerifyLinkRepository;
import com.example.verification.repository.VerifyRecordRepository;
import com.example.verification.service.AuditLogService;
import com.example.verification.service.SystemSettingService;
import com.example.verification.service.VerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final SignRecordRepository signRecordRepository;
    private final VerifyLinkRepository verifyLinkRepository;
    private final VerifyRecordRepository verifyRecordRepository;
    private final Sm3DigestService sm3DigestService;
    private final Sm2VerifyService sm2VerifyService;
    private final AuditLogService auditLogService;
    private final SystemSettingService systemSettingService;


    @Override
    @Transactional
    public VerifyLinkResponse createVerifyLink(Long userId, CreateVerifyLinkRequest request) {
        SignRecord signRecord = signRecordRepository.findBySignNo(request.getSignNo())
                .orElseThrow(() -> new BusinessException(ApiCode.SIGN_RECORD_NOT_FOUND, "签名记录不存在"));
        if (!signRecord.getUser().getId().equals(userId)) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "无权为该签名记录生成验签链接");
        }

        LocalDateTime now = LocalDateTime.now();
        long verifyLinkExpireHours = systemSettingService.getVerifyLinkExpireHours();
        VerifyLink verifyLink = new VerifyLink();
        verifyLink.setSignRecord(signRecord);
        verifyLink.setVerifyToken(UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis());
        verifyLink.setRandomFactor(UUID.randomUUID().toString().replace("-", ""));
        verifyLink.setTimeFactor(String.valueOf(System.currentTimeMillis()));
        verifyLink.setStatus(VerifyLinkStatus.ACTIVE);
        verifyLink.setExpiresAt(now.plusHours(verifyLinkExpireHours));
        verifyLink.setCurrentAccessCount(0);
        verifyLink.setCreatedAt(now);
        verifyLink.setUpdatedAt(now);
        verifyLinkRepository.save(verifyLink);
        auditLogService.saveLog("VERIFY", "CREATE_VERIFY_LINK", "创建验签链接", OperationResult.SUCCESS, "验签链接创建成功", signRecord.getUser().getId(), signRecord.getUser().getUsername());

        return VerifyLinkResponse.builder()
                .signNo(signRecord.getSignNo())
                .verifyToken(verifyLink.getVerifyToken())
                .verifyUrl("/verify/" + verifyLink.getVerifyToken())
                .build();
    }

    @Override
    @Transactional
    public VerifyResultResponse verifyFile(String verifyToken, MultipartFile file, String clientIp) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ApiCode.VERIFY_FILE_EMPTY, "上传文件不能为空");
        }
        validateUploadFileSize(file);

        VerifyLink verifyLink = verifyLinkRepository.findByVerifyToken(verifyToken)
                .orElseThrow(() -> new BusinessException(ApiCode.VERIFY_LINK_INVALID, "验签链接不存在或已失效"));

        if (verifyLink.getStatus() == VerifyLinkStatus.DISABLED) {
            throw new BusinessException(ApiCode.VERIFY_LINK_INVALID, "验签链接不存在或已失效");
        }
        if (isExpired(verifyLink)) {
            markExpiredIfNecessary(verifyLink);
            throw new BusinessException(ApiCode.VERIFY_LINK_INVALID, "验签链接不存在或已失效");
        }
        if (isAccessLimitExceeded(verifyLink)) {
            throw new BusinessException(ApiCode.VERIFY_LINK_INVALID, "验签链接访问次数已达上限");
        }

        SignRecord signRecord = verifyLink.getSignRecord();
        byte[] fileBytes = getFileBytes(file);
        String fileDigest = sm3DigestService.digestHex(fileBytes);
        boolean digestMatched = signRecord.getFileDigest().equalsIgnoreCase(fileDigest);
        boolean sm2Verified = digestMatched && sm2VerifyService.verifyHex(signRecord.getPublicKey(), fileDigest, signRecord.getSignatureValue());
        VerifyResult verifyResult = sm2Verified ? VerifyResult.PASSED : (digestMatched ? VerifyResult.FAILED : VerifyResult.FILE_MISMATCH);
        String resultMessage = sm2Verified ? "SM3 摘要匹配且 SM2 验签通过" : (digestMatched ? "SM3 摘要匹配，但 SM2 验签失败" : "文件摘要不匹配，验签失败");
        LocalDateTime now = LocalDateTime.now();

        VerifyRecord verifyRecord = new VerifyRecord();
        verifyRecord.setVerifyNo("VERIFY-" + UUID.randomUUID().toString().replace("-", ""));
        verifyRecord.setSignRecord(signRecord);
        verifyRecord.setVerifyLink(verifyLink);
        verifyRecord.setVerifyMode("PUBLIC_LINK");
        verifyRecord.setOperatorIp(clientIp);
        verifyRecord.setUploadFileName(file.getOriginalFilename());
        verifyRecord.setUploadFileSize(file.getSize());
        verifyRecord.setUploadFileDigest(fileDigest);
        verifyRecord.setVerifyResult(verifyResult);
        verifyRecord.setResultMessage(resultMessage);
        verifyRecord.setVerifyTime(now);
        verifyRecord.setSignerName(signRecord.getSignerName());
        verifyRecord.setOriginalSignTime(signRecord.getSignTime());
        verifyRecord.setPublicKey(signRecord.getPublicKey());
        verifyRecord.setCreatedAt(now);
        verifyRecord.setUpdatedAt(now);
        verifyRecordRepository.save(verifyRecord);

        verifyLink.setCurrentAccessCount(verifyLink.getCurrentAccessCount() + 1);
        verifyLink.setLastAccessAt(now);
        verifyLink.setLastAccessIp(clientIp);
        verifyLink.setUpdatedAt(now);
        verifyLinkRepository.save(verifyLink);
        auditLogService.saveLog("VERIFY", "VERIFY_FILE", "执行文件验签", verifyResult == VerifyResult.PASSED ? OperationResult.SUCCESS : OperationResult.FAILURE, resultMessage, signRecord.getUser().getId(), signRecord.getUser().getUsername());

        return VerifyResultResponse.builder()
                .signNo(signRecord.getSignNo())
                .signerName(signRecord.getSignerName())
                .verifyResult(verifyResult.name())
                .resultMessage(resultMessage)
                .digestAlgorithm(signRecord.getDigestAlgorithm())
                .fileDigest(fileDigest)
                .signatureAlgorithm(signRecord.getSignatureAlgorithm())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerifyLinkItemResponse> listUserVerifyLinks(Long userId) {
        return verifyLinkRepository.findBySignRecordUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(link -> VerifyLinkItemResponse.builder()
                        .signNo(link.getSignRecord().getSignNo())
                        .verifyToken(link.getVerifyToken())
                        .verifyUrl("/verify/" + link.getVerifyToken())
                        .status(resolveStatus(link).name())
                        .currentAccessCount(link.getCurrentAccessCount())
                        .maxAccessCount(systemSettingService.getVerifyLinkMaxAccessCount())
                        .expiresAt(link.getExpiresAt())
                        .lastAccessAt(link.getLastAccessAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerifyRecordItemResponse> listUserVerifyRecords(Long userId) {
        return verifyRecordRepository.findBySignRecordUserIdOrderByVerifyTimeDesc(userId)
                .stream()
                .map(record -> VerifyRecordItemResponse.builder()
                        .verifyNo(record.getVerifyNo())
                        .signNo(record.getSignRecord().getSignNo())
                        .verifyMode(record.getVerifyMode())
                        .uploadFileName(record.getUploadFileName())
                        .uploadFileDigest(record.getUploadFileDigest())
                        .verifyResult(record.getVerifyResult().name())
                        .resultMessage(record.getResultMessage())
                        .signerName(record.getSignerName())
                        .verifyTime(record.getVerifyTime())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerifyRecordItemResponse> listAllVerifyRecords() {
        return verifyRecordRepository.findAllByOrderByVerifyTimeDesc()
                .stream()
                .map(record -> VerifyRecordItemResponse.builder()
                        .verifyNo(record.getVerifyNo())
                        .signNo(record.getSignRecord().getSignNo())
                        .verifyMode(record.getVerifyMode())
                        .uploadFileName(record.getUploadFileName())
                        .uploadFileDigest(record.getUploadFileDigest())
                        .verifyResult(record.getVerifyResult().name())
                        .resultMessage(record.getResultMessage())
                        .signerName(record.getSignerName())
                        .verifyTime(record.getVerifyTime())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VerifyLinkDetailResponse getVerifyLinkDetail(Long userId, String verifyToken) {
        VerifyLink link = getOwnedVerifyLink(userId, verifyToken);
        return VerifyLinkDetailResponse.builder()
                .signNo(link.getSignRecord().getSignNo())
                .fileName(link.getSignRecord().getFileName())
                .signerName(link.getSignRecord().getSignerName())
                .verifyToken(link.getVerifyToken())
                .verifyUrl("/verify/" + link.getVerifyToken())
                .status(resolveStatus(link).name())
                .randomFactor(link.getRandomFactor())
                .timeFactor(link.getTimeFactor())
                .currentAccessCount(link.getCurrentAccessCount())
                .maxAccessCount(systemSettingService.getVerifyLinkMaxAccessCount())
                .expiresAt(link.getExpiresAt())
                .lastAccessAt(link.getLastAccessAt())
                .createdAt(link.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void disableVerifyLink(Long userId, String verifyToken) {
        VerifyLink link = getOwnedVerifyLink(userId, verifyToken);
        if (link.getStatus() == VerifyLinkStatus.DISABLED) {
            return;
        }
        link.setStatus(VerifyLinkStatus.DISABLED);
        link.setUpdatedAt(LocalDateTime.now());
        verifyLinkRepository.save(link);
        auditLogService.saveLog("VERIFY", "DISABLE_VERIFY_LINK", "禁用验签链接", OperationResult.SUCCESS, "验签链接已禁用", link.getSignRecord().getUser().getId(), link.getSignRecord().getUser().getUsername());
    }

    private VerifyLink getOwnedVerifyLink(Long userId, String verifyToken) {
        VerifyLink link = verifyLinkRepository.findByVerifyToken(verifyToken)
                .orElseThrow(() -> new BusinessException(ApiCode.VERIFY_LINK_INVALID, "验签链接不存在或已失效"));
        if (!link.getSignRecord().getUser().getId().equals(userId)) {
            throw new BusinessException(ApiCode.UNAUTHORIZED, "无权访问该验签链接");
        }
        return link;
    }

    private VerifyLinkStatus resolveStatus(VerifyLink link) {
        if (link.getStatus() == VerifyLinkStatus.DISABLED) {
            return VerifyLinkStatus.DISABLED;
        }
        if (isExpired(link)) {
            return VerifyLinkStatus.EXPIRED;
        }
        if (isAccessLimitExceeded(link)) {
            return VerifyLinkStatus.DISABLED;
        }
        return link.getStatus();
    }

    private boolean isExpired(VerifyLink link) {
        return link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private boolean isAccessLimitExceeded(VerifyLink link) {
        return link.getCurrentAccessCount() != null && link.getCurrentAccessCount() >= systemSettingService.getVerifyLinkMaxAccessCount();
    }

    private void markExpiredIfNecessary(VerifyLink link) {
        if (link.getStatus() == VerifyLinkStatus.ACTIVE && isExpired(link)) {
            link.setStatus(VerifyLinkStatus.EXPIRED);
            link.setUpdatedAt(LocalDateTime.now());
            verifyLinkRepository.save(link);
        }
    }

    private void validateUploadFileSize(MultipartFile file) {
        long maxFileSizeBytes = systemSettingService.getUploadMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException(ApiCode.FILE_SIZE_EXCEEDED, "上传文件大小超过限制，当前最大允许 " + systemSettingService.getUploadMaxFileSizeMb() + "MB");
        }
    }

    private byte[] getFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "读取上传文件失败");
        }
    }
}
