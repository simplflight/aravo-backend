package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.Activity;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.domain.enums.ActivityStatus;
import com.simplflight.aravo.dto.request.ActivityCompleteRequest;
import com.simplflight.aravo.dto.request.ActivityStartRequest;
import com.simplflight.aravo.dto.response.ActivityResponse;
import com.simplflight.aravo.engine.PointCalculationEngine;
import com.simplflight.aravo.event.ActivityCompletedEvent;
import com.simplflight.aravo.mapper.ActivityMapper;
import com.simplflight.aravo.repository.ActivityRepository;
import com.simplflight.aravo.repository.UserRepository;
import com.simplflight.aravo.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher eventPublisher;

    private final PointCalculationEngine pointEngine;
    private final MessageUtil messageUtil;
    private final ActivityMapper activityMapper;

    @Transactional
    public ActivityResponse startActivity(User user, ActivityStartRequest request) {

        boolean hasActive = activityRepository.existsByUserAndStatus(user, ActivityStatus.IN_PROGRESS);

        if (hasActive) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, messageUtil.get("error.activity.in.progress"));
        }

        Activity activity = Activity.builder()
                .user(user)
                .category(request.category())
                .status(ActivityStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        Activity savedActivity = activityRepository.save(activity);

        return activityMapper.toResponse(savedActivity);
    }

    @Transactional
    public ActivityResponse completeActivity(User user, UUID activityId, ActivityCompleteRequest request) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageUtil.get("error.activity.not.found")));

        if (!activity.getUser().getId().equals(user.getId()) || activity.getStatus() != ActivityStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, messageUtil.get("error.activity.not.in.progress"));
        }

        LocalDateTime now = LocalDateTime.now();
        long durationInMinutes = activity.calculateDurationInMinutes(now);

        int earnedPoints = pointEngine.calculatePoints((int) durationInMinutes, activity.getCategory(), now);
        activity.complete(now, earnedPoints, request.title(), request.description());

        if (earnedPoints > 0) {
            user.addPoints(earnedPoints);

            userRepository.save(user);

            // Todos os @EventListeners recebem esse objeto
            eventPublisher.publishEvent(new ActivityCompletedEvent(activity));
        }

        Activity savedActivity = activityRepository.save(activity);

        return activityMapper.toResponse(savedActivity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getUserActivities(User user) {

        List<Activity> activities = activityRepository.findAllByUserOrderByDateDesc(user);

        return activityMapper.toResponseList(activities);
    }

    @Transactional
    public ActivityResponse updateActivity(User user, UUID activityId, ActivityCompleteRequest request) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageUtil.get("error.activity.not.found")));

        if (!activity.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageUtil.get("error.forbidden"));
        }

        activity.setTitle(request.title());
        activity.setDescription(request.description());

        return activityMapper.toResponse(activityRepository.save(activity));
    }

    @Transactional
    public void deleteActivity(User user, UUID activityId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, messageUtil.get("error.activity.not.found")));

        if (!activity.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, messageUtil.get("error.forbidden"));
        }

        activityRepository.delete(activity);
    }
}
