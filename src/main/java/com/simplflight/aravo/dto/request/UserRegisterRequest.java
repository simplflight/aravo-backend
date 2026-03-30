package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

        @NotBlank(message = "O email é obrigatório.")
        @Email(message = "O formato do email é inválido.")
        String email,

        @NotBlank(message = "O nickname é obrigatório.")
        @Size(min = 3, max = 100, message = "O nickname deve ter entre 3 e 100 caracteres.")
        String nickname,

        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve possuir, pelo menos, 6 caracteres.")
        String password
) {
}
