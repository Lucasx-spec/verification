package com.example.verification.repository;

import com.example.verification.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByChainIndexAsc();

    List<AuditLog> findAllByOrderByOperationTimeDesc();

    List<AuditLog> findByUserIdOrderByOperationTimeDesc(Long userId);
}
