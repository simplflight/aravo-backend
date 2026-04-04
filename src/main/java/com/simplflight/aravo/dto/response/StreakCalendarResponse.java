package com.simplflight.aravo.dto.response;

import java.util.List;

public record StreakCalendarResponse(

        Integer currentStreak,
        Integer highestStreak,
        List<DailyRecord> history
) {

    public record DailyRecord(

            String date,
            Integer count,
            String status
    ) {}
}
