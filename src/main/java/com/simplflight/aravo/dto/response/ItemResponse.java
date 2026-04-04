package com.simplflight.aravo.dto.response;

import com.simplflight.aravo.domain.enums.ItemType;

import java.io.Serializable;
import java.util.UUID;

public record ItemResponse(

        UUID id,
        String name,
        String description,
        Integer price,
        ItemType type,
        String iconKey,
        Integer maxQuantity
) implements Serializable {
}
