package com.simplflight.aravo.engine;

import com.simplflight.aravo.domain.entity.Campaign;
import com.simplflight.aravo.domain.enums.ActivityCategory;
import com.simplflight.aravo.repository.CampaignRepository;
import com.simplflight.aravo.service.strategy.CampaignStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class XpCalculationEngine {

    private static final int MAX_REWARDABLE_MINUTES = 120;

    private final CampaignRepository campaignRepository;
    private final CampaignStrategy campaignStrategy;

    /**
     * Calcula o XP final da atividade cruzando o tempo com os multiplicadores.
     */
    public int calculateXp(int focusTimeMinutes, ActivityCategory category, LocalDateTime activityDate) {

        if (focusTimeMinutes < 2) {
            return 0;
        }

        int baseXp = calculateBaseXp(focusTimeMinutes);

        double multiplier = calculateMultiplier(category, activityDate);

        return (int) Math.round(baseXp * multiplier);
    }

    /**
     * Lógica progressiva de ganho de XP:
     * Até 30 min: 1 XP a cada 2 min.
     * De 30 a 50 min: 3 XP a cada 2 min.
     * De 50 a 120 min: 5 XP a cada 2 min.
     */
    private int calculateBaseXp(int minutes) {

        int xp = 0;
        int effectiveMinutes = Math.min(minutes, MAX_REWARDABLE_MINUTES);

        // Tier 1: 0 a 30 minutos
        int tier1Minutes = Math.min(effectiveMinutes, 30);
        xp += tier1Minutes / 2;

        // Tier 2: 30 a 50 minutos
        if (effectiveMinutes > 30) {
            int tier2Minutes = Math.min(effectiveMinutes - 30, 20);
            xp += (tier2Minutes / 2) * 3;
        }

        // Tier 3: 50+ minutos
        if (effectiveMinutes > 50) {
            int tier3Minutes = effectiveMinutes - 50;
            xp += (tier3Minutes / 2) * 5;
        }

        return xp;
    }

    /**
     * Calcula os bônus contextuais de forma cumulativa e dinâmica.
     */
    private double calculateMultiplier(ActivityCategory category, LocalDateTime date) {

        // Bônus de final de semana (+20%)
        double weekendBonus = 0.0;
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            weekendBonus = 0.20;
        }

        // Busca campanhas ativas.
        List<Campaign> activeCampaigns = campaignRepository.findValidCampaigns(date, category);

        // Delega o cálculo e o limite de bônus.
        double campaignMultiplier = campaignStrategy.calculateMultiplier(activeCampaigns);

        // Retorna a composição final dos multiplicadores.
        return campaignMultiplier + weekendBonus;
    }
}
