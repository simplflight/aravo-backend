package com.simplflight.aravo.engine;

import com.simplflight.aravo.domain.enums.ActivityCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointCalculationEngineTest {

    private PointCalculationEngine engine;
    private LocalDateTime standardMonday;

    @BeforeEach
    void setUp() {

        engine = new PointCalculationEngine();

        // Um dia útil comum, fora de campanhas
        standardMonday = LocalDateTime.of(2026, 4, 6, 10, 0);
    }

    @Test
    @DisplayName("Deve retornar 0 pontos para atividades menores que 2 minutos")
    void testIgnoredShortActivities() {
        // Arrange
        int focusTime = 1;

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.STUDY, standardMonday);

        // Assert
        assertEquals(0, points);
    }

    @Test
    @DisplayName("Tier 1: Deve calcular 1 ponto a cada 2 minutos (até 30m)")
    void testTier1Calculation() {
        // Arrange
        int focusTime = 20;

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.STUDY, standardMonday);

        // Assert (20 minutos / 2 = 10 pontos)
        assertEquals(10, points);
    }

    @Test
    @DisplayName("Tier 2: Deve calcular pontos progressivos para 30 a 50 minutos")
    void testTier2Calculation() {
        // Arrange
        int focusTime = 40;

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.STUDY, standardMonday);

        // Assert (30m no Tier 1 (15) + 10m no Tier 2 (15) = 30 pontos)
        assertEquals(30, points);
    }

    @Test
    @DisplayName("Tier 3: Deve calcular pontos progressivos máximos acima de 50 minutos")
    void testTier3Calculation() {
        // Arrange
        int focusTime = 60;

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.STUDY, standardMonday);

        // Assert (30m Tier 1 (15) + 20m Tier 2 (30) + 10m Tier 3 (25) = 70 pontos)
        assertEquals(70, points);
    }

    @Test
    @DisplayName("Limite: Não deve pontuar acima do teto de 120 minutos (MAX_REWARDABLE_MINUTES)")
    void testMaxMinutesCap() {
        // Arrange
        int focusTimeAtLimit = 120;
        int focusTimeBeyondLimit = 150;

        // Act
        int pointsAt120 = engine.calculatePoints(focusTimeAtLimit, ActivityCategory.STUDY, standardMonday);
        int pointsAt150 = engine.calculatePoints(focusTimeBeyondLimit, ActivityCategory.STUDY, standardMonday);

        // Assert (Teto de 120m deve gerar 220 pontos base)
        assertEquals(220, pointsAt120);
        assertEquals(220, pointsAt150, "O tempo extra não deve gerar mais pontos");
    }

    @Test
    @DisplayName("Multiplicador: Deve adicionar 20% aos finais de semana")
    void testWeekendMultiplier() {
        // Arrange
        int focusTime = 20;
        LocalDateTime saturday = LocalDateTime.of(2026, 4, 11, 10, 0);

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.STUDY, saturday);

        // Assert (10 base + 20% = 12 pontos)
        assertEquals(12, points);
    }

    @Test
    @DisplayName("Campanha: Deve adicionar 50% para HEALTH em Outubro de 2026")
    void testHealthCampaignBonus() {
        // Arrange
        int focusTime = 20;
        LocalDateTime oct2026 = LocalDateTime.of(2026, 10, 15, 10, 0);

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.HEALTH, oct2026);

        // Assert (10 base + 50% = 15 pontos)
        assertEquals(15, points);
    }

    @Test
    @DisplayName("Acúmulo: Dia do Programador no Fim de Semana (Bônus Combinado)")
    void testProgrammerDayOnWeekendBonus() {
        // Arrange
        int focusTime = 20;
        LocalDateTime programmerDayWeekend = LocalDateTime.of(2026, 9, 13, 10, 0);

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.WORK, programmerDayWeekend);

        // Assert (Base(1.0) + Weekend(0.2) + Programmer(1.0) = 2.2 -> 10 * 2.2 = 22 pontos)
        assertEquals(22, points);
    }
}