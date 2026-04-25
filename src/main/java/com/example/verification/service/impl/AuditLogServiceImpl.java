package com.example.verification.service.impl;

import com.example.verification.model.entity.AuditLog;
import com.example.verification.model.enums.OperationResult;
import com.example.verification.model.vo.AuditIntegrityCheckResponse;
import com.example.verification.model.vo.AuditIntegrityIssueResponse;
import com.example.verification.model.vo.AuditLogItemResponse;
import com.example.verification.repository.AuditLogRepository;
import com.example.verification.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog saveLog(String module, String operationType, String operationDesc, OperationResult operationResult, String resultMessage, Long userId, String username) {
        List<AuditLog> allLogs = auditLogRepository.findAllByOrderByChainIndexAsc();
        AuditLog lastLog = allLogs.isEmpty() ? null : allLogs.get(allLogs.size() - 1);
        String prevHash = lastLog == null ? null : lastLog.getCurrentHash();
        LocalDateTime now = LocalDateTime.now();

        AuditLog auditLog = new AuditLog();
        auditLog.setLogNo("LOG-" + UUID.randomUUID().toString().replace("-", ""));
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setModule(module);
        auditLog.setOperationType(operationType);
        auditLog.setOperationDesc(operationDesc);
        auditLog.setOperationResult(operationResult);
        auditLog.setResultMessage(resultMessage);
        auditLog.setLogContentHash(calculateContentHash(auditLog));
        auditLog.setPrevHash(prevHash);
        auditLog.setCurrentHash(calculateChainHash(prevHash, auditLog.getLogContentHash()));
        auditLog.setChainIndex(lastLog == null ? 1L : lastLog.getChainIndex() + 1);
        auditLog.setOperationTime(now);
        auditLog.setCreatedAt(now);
        auditLog.setUpdatedAt(now);
        return auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLogItemResponse> listUserLogs(Long userId) {
        return auditLogRepository.findByUserIdOrderByOperationTimeDesc(userId)
                .stream()
                .map(this::toItemResponse)
                .toList();
    }

    @Override
    public List<AuditLogItemResponse> listAllLogs() {
        return auditLogRepository.findAllByOrderByOperationTimeDesc()
                .stream()
                .map(this::toItemResponse)
                .toList();
    }

    @Override
    public AuditIntegrityCheckResponse checkIntegrity() {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByChainIndexAsc();
        List<AuditIntegrityIssueResponse> issues = new ArrayList<>();

        for (int i = 0; i < logs.size(); i++) {
            AuditLog current = logs.get(i);
            AuditLog previous = i == 0 ? null : logs.get(i - 1);

            long expectedChainIndex = i + 1L;
            if (current.getChainIndex() == null || !current.getChainIndex().equals(expectedChainIndex)) {
                issues.add(buildIssue(current, "CHAIN_INDEX_MISMATCH", "链序号不连续或为空"));
            }

            String expectedContentHash = calculateContentHash(current);
            if (!expectedContentHash.equals(current.getLogContentHash())) {
                issues.add(buildIssue(current, "CONTENT_HASH_MISMATCH", "日志内容哈希不匹配"));
            }

            String expectedPrevHash = previous == null ? null : previous.getCurrentHash();
            if (previous == null) {
                if (current.getPrevHash() != null) {
                    issues.add(buildIssue(current, "GENESIS_PREV_HASH_INVALID", "首条日志的 prevHash 应为空"));
                }
            } else if (!expectedPrevHash.equals(current.getPrevHash())) {
                issues.add(buildIssue(current, "PREV_HASH_MISMATCH", "prevHash 与上一条 currentHash 不一致"));
            }

            String expectedCurrentHash = calculateChainHash(current.getPrevHash(), current.getLogContentHash());
            if (!expectedCurrentHash.equals(current.getCurrentHash())) {
                issues.add(buildIssue(current, "CURRENT_HASH_MISMATCH", "currentHash 校验失败"));
            }
        }

        return AuditIntegrityCheckResponse.builder()
                .valid(issues.isEmpty())
                .totalLogs(logs.size())
                .issueCount(issues.size())
                .latestHash(logs.isEmpty() ? null : logs.get(logs.size() - 1).getCurrentHash())
                .issues(issues)
                .build();
    }

    private AuditLogItemResponse toItemResponse(AuditLog log) {
        return AuditLogItemResponse.builder()
                .logNo(log.getLogNo())
                .module(log.getModule())
                .operationType(log.getOperationType())
                .operationDesc(log.getOperationDesc())
                .operationResult(log.getOperationResult().name())
                .resultMessage(log.getResultMessage())
                .operationTime(log.getOperationTime())
                .chainIndex(log.getChainIndex())
                .build();
    }

    private AuditIntegrityIssueResponse buildIssue(AuditLog log, String issueType, String message) {
        return AuditIntegrityIssueResponse.builder()
                .chainIndex(log.getChainIndex())
                .logNo(log.getLogNo())
                .issueType(issueType)
                .message(message)
                .build();
    }

    private String calculateContentHash(AuditLog log) {
        return hash(log.getModule()
                + log.getOperationType()
                + log.getOperationDesc()
                + log.getOperationResult()
                + log.getResultMessage()
                + log.getUserId()
                + log.getUsername());
    }

    private String calculateChainHash(String prevHash, String contentHash) {
        return hash((prevHash == null ? "GENESIS" : prevHash) + contentHash);
    }

    private String hash(String content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
