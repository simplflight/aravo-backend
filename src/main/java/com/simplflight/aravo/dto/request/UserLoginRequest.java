package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(

        @NotBlank(message = "{user.identifier.required}")
        String identifier,

        @NotBlank(message = "{user.password.required}")
        String password
) {
}
