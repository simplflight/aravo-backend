package com.simplflight.aravo.service.strategy;

import com.simplflight.aravo.domain.entity.Campaign;

import java.util.List;

public interface CampaignStrategy {

    double calculateMultiplier(List<Campaign> activeCampaigns);
}
