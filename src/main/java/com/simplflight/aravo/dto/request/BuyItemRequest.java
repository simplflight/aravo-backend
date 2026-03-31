package com.simplflight.aravo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BuyItemRequest(

        @NotNull(message = "{item.id.required}")
        UUID itemId,

        @NotNull(message = "{item.quantity.required}")
        @Min(value = 1, message = "{item.quantity.min}")
        Integer quantity
) {
}
