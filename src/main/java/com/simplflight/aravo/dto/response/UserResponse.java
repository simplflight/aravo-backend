package com.simplflight.aravo.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(

        UUID id,
        String email,
        String nickname,
        String name,
        Integer points,
        Integer totalPoints,
        Integer streak,
        Integer highestStreak,
        LocalDateTime createdAt,
        LocalDate lastActivityDate
) {
}
