package com.simplflight.aravo.dto.request;

import com.simplflight.aravo.domain.enums.ActivityCategory;
import jakarta.validation.constraints.NotNull;

public record ActivityStartRequest(

        @NotNull(message = "{activity.category.required}")
        ActivityCategory category
) {
}
