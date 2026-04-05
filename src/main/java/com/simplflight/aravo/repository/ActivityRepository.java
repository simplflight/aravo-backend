package com.simplflight.aravo.repository;

import com.simplflight.aravo.domain.entity.Activity;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.domain.enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findAllByUserOrderByDateDesc(User user);

    boolean existsByUserAndStatus(User user, ActivityStatus status);

    void deleteByUser(User user);
}
