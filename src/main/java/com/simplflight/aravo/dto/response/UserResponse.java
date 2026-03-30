package com.simplflight.aravo.dto.response;

import java.time.LocalDate;
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
        Integer registrationYear,
        Integer focusPreference,
        Integer restPreference,
        LocalDate lastActivityDate
) {
}
