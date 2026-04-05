package com.simplflight.aravo.controller;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.response.InventoryResponse;
import com.simplflight.aravo.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getInventory(@AuthenticationPrincipal User currentUser) {

        List<InventoryResponse> responses = inventoryService.getUserInventory(currentUser);

        return ResponseEntity.ok(responses);
    }
}
