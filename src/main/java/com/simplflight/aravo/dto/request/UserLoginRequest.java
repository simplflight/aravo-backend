package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(

        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        String email,

        @NotBlank(message = "{user.password.required}")
        String password
) {
}
