package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.Activity;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.domain.enums.ActivityStatus;
import com.simplflight.aravo.dto.request.ActivityCompleteRequest;
import com.simplflight.aravo.dto.request.ActivityStartRequest;
import com.simplflight.aravo.dto.response.ActivityResponse;
import com.simplflight.aravo.engine.PointCalculationEngine;
import com.simplflight.aravo.repository.ActivityRepository;
import com.simplflight.aravo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    private final PointCalculationEngine pointEngine;
    private final StreakService streakService;
    private final MessageSource messageSource;

    @Transactional
    public ActivityResponse startActivity(User user, ActivityStartRequest request) {

        boolean hasActive = activityRepository.existsByUserAndStatus(user, ActivityStatus.IN_PROGRESS);

        if (hasActive) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, getMessage("error.activity.in.progress"));
        }

        Activity activity = Activity.builder()
                .user(user)
                .category(request.category())
                .status(ActivityStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        Activity saved = activityRepository.save(activity);

        return mapToResponse(saved);
    }

    @Transactional
    public ActivityResponse completeActivity(User user, UUID activityId, ActivityCompleteRequest request) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, getMessage("error.activity.not.found")));

        if (!activity.getUser().getId().equals(user.getId()) || activity.getStatus() != ActivityStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, getMessage("error.activity.not.in.progress"));
        }

        LocalDateTime now = LocalDateTime.now();

        long durationInMinutes = activity.calculateDurationInMinutes(now);

        int earnedPoints = pointEngine.calculatePoints((int) durationInMinutes, activity.getCategory(), now);

        activity.complete(now, earnedPoints, request.title(), request.description());

        if (earnedPoints > 0) {
            user.addPoints(earnedPoints);

            streakService.recordActivityToday(user, now.toLocalDate());

            userRepository.save(user);
        }

        return mapToResponse(activityRepository.save(activity));
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
                activity.getCategory(),
                activity.getStatus(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getPointsEarned()
        );
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
