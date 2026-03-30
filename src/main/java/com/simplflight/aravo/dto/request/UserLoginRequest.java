package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(

        @NotBlank(message = "O email é obrigatório.")
        @Email(message = "O formato do email é inválido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        String password
) {
}
