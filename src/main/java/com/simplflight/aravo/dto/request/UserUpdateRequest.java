package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Size(min = 3, max = 20, message = "{user.nickname.size}")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "{user.nickname.pattern}")
        String nickname,

        String name
) {
}
