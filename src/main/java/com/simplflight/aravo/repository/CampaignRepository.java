package com.simplflight.aravo.repository;

import com.simplflight.aravo.domain.entity.Campaign;
import com.simplflight.aravo.domain.enums.ActivityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    @Query("SELECT c FROM Campaign c WHERE c.isActive = true " +
            "AND c.startDate <= :now AND c.endDate >= :now " +
            "AND (c.category IS NULL OR c.category = :category)")
    List<Campaign> findValidCampaigns(
            @Param("now") LocalDateTime now,
            @Param("category") ActivityCategory category
    );
}
