package com.simplflight.aravo.controller;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.BuyItemRequest;
import com.simplflight.aravo.dto.response.ItemResponse;
import com.simplflight.aravo.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems() {

        List<ItemResponse> items = itemService.getAllItems();

        return ResponseEntity.ok(items);
    }

    @PostMapping("/buy")
    public ResponseEntity<Void> buyItem(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody BuyItemRequest request
    ) {

        itemService.buyItem(currentUser, request);

        return ResponseEntity.ok().build();
    }
}