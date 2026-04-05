package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ActivityCompleteRequest(

        @NotBlank(message = "{activity.title.required}")
        String title,

        String description
) {
}
