package com.simplflight.aravo.service.strategy;

import com.simplflight.aravo.domain.entity.Campaign;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdditiveCampaignStrategy implements CampaignStrategy {

    private static final double MAX_MULTIPLIER = 3.0;
    private static final double BASE_MULTIPLIER = 1.0;

    @Override
    public double calculateMultiplier(List<Campaign> activeCampaigns) {

        if (activeCampaigns == null || activeCampaigns.isEmpty()) {
            return BASE_MULTIPLIER;
        }

        double totalBonus = activeCampaigns.stream()
                .mapToDouble(Campaign::getBonus)
                .sum();

        double finalMultiplier = BASE_MULTIPLIER + totalBonus;

        return Math.min(finalMultiplier, MAX_MULTIPLIER);
    }
}
