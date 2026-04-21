package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "{user.refreshtoken.required}")
        String refreshToken
) {
}
