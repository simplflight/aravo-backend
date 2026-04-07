package com.simplflight.aravo.dto.response;

import com.simplflight.aravo.domain.enums.ActivityCategory;
import com.simplflight.aravo.domain.enums.ActivityStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityResponse(

        UUID id,
        ActivityCategory category,
        ActivityStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer xpEarned
) {
}
