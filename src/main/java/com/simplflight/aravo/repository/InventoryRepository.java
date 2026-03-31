package com.simplflight.aravo.repository;

import com.simplflight.aravo.domain.entity.Inventory;
import com.simplflight.aravo.domain.entity.Item;
import com.simplflight.aravo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByUserAndItem(User user, Item item);
}
