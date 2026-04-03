package com.simplflight.aravo.dto.request;

import com.simplflight.aravo.domain.enums.ActivityCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivityRegisterRequest(

        @NotBlank(message = "{activity.title.required}")
        String title,

        String description,

        @NotNull(message = "{activity.category.required}")
        ActivityCategory category,

        @NotNull(message = "{activity.focustime.required}")
        @Min(value = 2, message = "{activity.focustime.min}")
        Integer focusTime
) {
}
