package com.example.verification.repository;

import com.example.verification.model.entity.SignRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignRecordRepository extends JpaRepository<SignRecord, Long> {

    Optional<SignRecord> findBySignNo(String signNo);

    List<SignRecord> findAllByOrderBySignTimeDesc();

    List<SignRecord> findByUserIdOrderBySignTimeDesc(Long userId);
}
