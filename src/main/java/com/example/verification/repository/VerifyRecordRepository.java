package com.example.verification.repository;

import com.example.verification.model.entity.VerifyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerifyRecordRepository extends JpaRepository<VerifyRecord, Long> {

    List<VerifyRecord> findAllByOrderByVerifyTimeDesc();

    List<VerifyRecord> findBySignRecordUserIdOrderByVerifyTimeDesc(Long userId);
}
