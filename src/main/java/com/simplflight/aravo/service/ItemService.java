package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.Inventory;
import com.simplflight.aravo.domain.entity.Item;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.BuyItemRequest;
import com.simplflight.aravo.dto.response.ItemResponse;
import com.simplflight.aravo.repository.InventoryRepository;
import com.simplflight.aravo.repository.ItemRepository;
import com.simplflight.aravo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll().stream()
                .map(item -> new ItemResponse(
                        item.getId(),
                        item.getName(),
                        item.getDescription(),
                        item.getPrice(),
                        item.getType(),
                        item.getIconKey(),
                        item.getMaxQuantity()
                )).toList();
    }

    @Transactional
    public void buyItem(User currentUser, BuyItemRequest request) {

        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, getMessage("error.item.not.found")));

        int totalCost = item.getPrice() * request.quantity();

        if (currentUser.getPoints() < totalCost) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, getMessage("error.insufficient.points"));
        }

        Inventory inventory = inventoryRepository.findByUserAndItem(currentUser, item)
                .orElse(Inventory.builder()
                        .user(currentUser)
                        .item(item)
                        .quantity(0)
                        .build());

        int newQuantity = inventory.getQuantity() + request.quantity();
        if (newQuantity > item.getMaxQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, getMessage("error.item.max.quantity"));
        }

        currentUser.setPoints(currentUser.getPoints() - totalCost);
        userRepository.save(currentUser);

        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
