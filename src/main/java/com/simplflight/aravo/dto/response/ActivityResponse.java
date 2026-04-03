package com.simplflight.aravo.dto.response;

import com.simplflight.aravo.domain.enums.ActivityCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityResponse(

        UUID id,
        String title,
        String description,
        ActivityCategory category,
        Integer focusTime,
        Integer points,
        LocalDateTime date
) {
}
