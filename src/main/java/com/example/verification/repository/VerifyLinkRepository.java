package com.example.verification.repository;

import com.example.verification.model.entity.VerifyLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerifyLinkRepository extends JpaRepository<VerifyLink, Long> {

    Optional<VerifyLink> findByVerifyToken(String verifyToken);

    List<VerifyLink> findBySignRecordUserIdOrderByCreatedAtDesc(Long userId);
}
