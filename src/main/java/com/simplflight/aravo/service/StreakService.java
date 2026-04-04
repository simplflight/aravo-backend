package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.domain.entity.UserDailyTracking;
import com.simplflight.aravo.domain.enums.DailyTrackingStatus;
import com.simplflight.aravo.domain.enums.ItemType;
import com.simplflight.aravo.dto.response.StreakCalendarResponse;
import com.simplflight.aravo.repository.InventoryRepository;
import com.simplflight.aravo.repository.UserDailyTrackingRepository;
import com.simplflight.aravo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserDailyTrackingRepository trackingRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public StreakCalendarResponse getCalendar(User user, int month, int year) {

        evaluatePastStreak(user, LocalDate.now());

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<UserDailyTracking> trackings = trackingRepository
                .findByUserAndTrackingDateBetweenOrderByTrackingDateAsc(user, start, end);

        List<StreakCalendarResponse.DailyRecord> history = trackings.stream()
                .map(t -> new StreakCalendarResponse.DailyRecord(
                        t.getTrackingDate().toString(),
                        t.getActivitiesCount(),
                        t.getStatus().name()
                )).toList();

        return new StreakCalendarResponse(
                user.getStreak(),
                user.getHighestStreak(),
                history
        );
    }

    @Transactional
    public void recordActivityToday(User user, LocalDate today) {

        evaluatePastStreak(user, today);

        UserDailyTracking tracking = trackingRepository.findByUserAndTrackingDate(user, today)
                .orElse(UserDailyTracking.builder()
                        .user(user)
                        .trackingDate(today)
                        .activitiesCount(0)
                        .status(DailyTrackingStatus.COMPLETED)
                        .build());

        tracking.setActivitiesCount(tracking.getActivitiesCount() + 1);

        trackingRepository.save(tracking);

        if (!today.equals(user.getLastActivityDate())) {
            user.setStreak(user.getStreak() + 1);
            user.setLastActivityDate(today);

            if (user.getStreak() > user.getHighestStreak()) {
                user.setHighestStreak(user.getStreak());
            }

            userRepository.save(user);
        }
    }

    @Transactional
    public void evaluatePastStreak(User user, LocalDate today) {

        LocalDate lastActivity = user.getLastActivityDate();
        if (lastActivity == null) return;

        long missedDays = ChronoUnit.DAYS.between(lastActivity, today) - 1;

        if (missedDays <= 0 || user.getStreak() == 0) return;

        for (int i = 1; i <= missedDays; i++) {
            LocalDate missedDate = lastActivity.plusDays(i);

            boolean recovered = tryConsumeStreakFreeze(user);

            if (recovered) {
                UserDailyTracking freezeTrack = UserDailyTracking.builder()
                        .user(user)
                        .trackingDate(missedDate)
                        .activitiesCount(0)
                        .status(DailyTrackingStatus.FROZEN)
                        .build();

                trackingRepository.save(freezeTrack);
            } else {
                user.setStreak(0);

                userRepository.save(user);

                break;
            }
        }

        if (user.getStreak() > 0) {
            user.setLastActivityDate(today.minusDays(1));

            userRepository.save(user);
        }
    }

    private boolean tryConsumeStreakFreeze(User user) {
        return inventoryRepository.findByUserAndItem_TypeAndQuantityGreaterThan(user, ItemType.STREAK_FREEZE, 0)
                .map(inv -> {
                    inv.setQuantity(inv.getQuantity() - 1);
                    inventoryRepository.save(inv);
                    return true;
                }).orElse(false);
    }
}