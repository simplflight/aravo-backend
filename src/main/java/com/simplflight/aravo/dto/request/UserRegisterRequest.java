package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        String email,

        @NotBlank(message = "{user.nickname.required}")
        @Size(min = 3, max = 100, message = "{user.nickname.size}")
        String nickname,

        @NotBlank(message = "{user.name.required}")
        String name,

        @NotBlank(message = "{user.password.required}")
        @Size(min = 6, message = "{user.password.size}")
        String password
) {
}
