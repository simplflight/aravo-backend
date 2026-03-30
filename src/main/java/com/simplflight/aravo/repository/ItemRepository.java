package com.simplflight.aravo.repository;

import com.simplflight.aravo.domain.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {
}
