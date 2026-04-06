package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(

        @NotBlank // fazer message
        String idToken
) {
}
