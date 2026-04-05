package com.simplflight.aravo.dto.response;

import java.util.UUID;

public record InventoryResponse(

        UUID itemId,
        String itemName,
        String iconKey,
        Integer currentQuantity,
        Integer maxQuantity
) {
}
