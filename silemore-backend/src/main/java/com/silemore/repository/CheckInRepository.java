package com.silemore.repository;

import com.silemore.entity.CheckIn;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    boolean existsByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);

    Optional<CheckIn> findByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);

    Optional<CheckIn> findTopByUserIdOrderByCheckInDateDesc(Long userId);

    java.util.List<CheckIn> findByUserIdOrderByCheckInDateDesc(Long userId);

    Page<CheckIn> findByUserIdAndCheckInDateBetween(Long userId, LocalDate start, LocalDate end,
                                                    Pageable pageable);

    Page<CheckIn> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
