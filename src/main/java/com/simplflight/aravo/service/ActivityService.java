package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.Activity;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.ActivityRegisterRequest;
import com.simplflight.aravo.dto.response.ActivityResponse;
import com.simplflight.aravo.engine.PointCalculationEngine;
import com.simplflight.aravo.repository.ActivityRepository;
import com.simplflight.aravo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final PointCalculationEngine pointEngine;

    @Transactional
    public ActivityResponse registerActivity(User user, ActivityRegisterRequest request) {

        LocalDateTime now = LocalDateTime.now();
        int earnedPoints = pointEngine.calculatePoints(request.focusTime(), request.category(), now);

        Activity activity = Activity.builder()
                .user(user)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .focusTime(request.focusTime())
                .points(earnedPoints)
                .date(now)
                .build();

        user.setPoints(user.getPoints() + earnedPoints);
        user.setTotalPoints(user.getTotalPoints() + earnedPoints);

        userRepository.save(user);
        Activity savedActivity = activityRepository.save(activity);

        return mapToResponse(savedActivity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getUserActivities(User user) {
        return activityRepository.findAllByUserOrderByDateDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ActivityResponse mapToResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getCategory(),
                activity.getFocusTime(),
                activity.getPoints(),
                activity.getDate()
        );
    }
}
