package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.response.InventoryResponse;
import com.simplflight.aravo.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> getUserInventory(User user) {
        return inventoryRepository.findAllByUser(user).stream()
                .map(inv -> new InventoryResponse(
                        inv.getItem().getId(),
                        inv.getItem().getName(),
                        inv.getItem().getIconKey(),
                        inv.getQuantity(),
                        inv.getItem().getMaxQuantity()
                ))
                .toList();
    }
}
