package com.silemore.repository;

import com.silemore.entity.EmergencyContact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    long countByUserId(Long userId);

    boolean existsByUserIdAndEmailIgnoreCase(Long userId, String email);

    List<EmergencyContact> findByUserId(Long userId);

    List<EmergencyContact> findByUserIdAndIsVerifiedTrue(Long userId);

    Optional<EmergencyContact> findByVerifyToken(String verifyToken);
}
