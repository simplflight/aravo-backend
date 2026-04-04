package com.simplflight.aravo.repository;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.domain.entity.UserDailyTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDailyTrackingRepository extends JpaRepository<UserDailyTracking, UUID> {

    Optional<UserDailyTracking> findByUserAndTrackingDate(User user, LocalDate date);

    List<UserDailyTracking> findByUserAndTrackingDateBetweenOrderByTrackingDateAsc(User user, LocalDate start, LocalDate end);
}
