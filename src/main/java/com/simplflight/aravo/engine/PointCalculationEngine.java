package com.simplflight.aravo.engine;

import com.simplflight.aravo.domain.enums.ActivityCategory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Component
public class PointCalculationEngine {

    private static final int MAX_REWARDABLE_MINUTES = 120;

    /**
     * Calcula os pontos finais da atividade cruzando o tempo com os multiplicadores.
     */
    public int calculatePoints(int focusTimeMinutes, ActivityCategory category, LocalDateTime activityDate) {

        if (focusTimeMinutes < 2) {
            return 0;
        }

        int basePoints = calculateBasePoints(focusTimeMinutes);

        double multiplier = calculateMultiplier(category, activityDate);

        return (int) Math.round(basePoints * multiplier);
    }

    /**
     * Lógica progressiva:
     * Até 30 min: 1 pt a cada 2 min.
     * De 30 a 50 min: 3 pts a cada 2 min.
     * De 50 a 120 min: 5 pts a cada 2 min.
     */
    private int calculateBasePoints(int minutes) {

        int points = 0;
        int effectiveMinutes = Math.min(minutes, MAX_REWARDABLE_MINUTES);

        // Tier 1: 0 a 30 minutos
        int tier1Minutes = Math.min(effectiveMinutes, 30);
        points += tier1Minutes / 2;

        // Tier 2: 30 a 50 minutos
        if (effectiveMinutes > 30) {
            int tier2Minutes = Math.min(effectiveMinutes - 30, 20);
            points += (tier2Minutes / 2) * 3;
        }

        // Tier 3: 50+ minutos
        if (effectiveMinutes > 50) {
            int tier3Minutes = effectiveMinutes - 50;
            points += (tier3Minutes / 2) * 5;
        }

        return points;
    }

    /**
     * Calcula os bônus contextuais de forma cumulativa.
     */
    private double calculateMultiplier(ActivityCategory category, LocalDateTime date) {

        double multiplier = 1.0;

        // Bônus de final de semana (+20%)
        DayOfWeek day  = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            multiplier += 0.20;
        }

        // Campanhas de incentivo
        multiplier += getDynamicCampaignBonus(category, date);

        return  multiplier;
    }

    /**
     * Verifica possíveis bônus em relação à campanhas de incentivo
     * TODO: repository.findActiveCampaign(category, date)
     */
    private double getDynamicCampaignBonus(ActivityCategory category, LocalDateTime date) {

        // Mês de combate à obesidade
        if (category == ActivityCategory.HEALTH && date.getMonthValue() == 10 && date.getYear() == 2026) {
            return 0.50;
        }

        // Dia do programador
        if ((category == ActivityCategory.WORK || category == ActivityCategory.STUDY) && date.getDayOfYear() == 256) {
            return 1.0;
        }

        return 0.0;
    }
}
