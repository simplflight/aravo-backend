package com.simplflight.aravo.engine;

import com.simplflight.aravo.domain.enums.ActivityCategory;
import com.simplflight.aravo.repository.CampaignRepository;
import com.simplflight.aravo.service.strategy.CampaignStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointCalculationEngineTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CampaignStrategy campaignStrategy;

    @InjectMocks
    private PointCalculationEngine engine;

    private LocalDateTime standardMonday;

    @BeforeEach
    void setUp() {
        // Um dia útil comum, fora de campanhas
        standardMonday = LocalDateTime.of(2026, 4, 6, 10, 0);

        lenient().when(campaignStrategy.calculateMultiplier(any())).thenReturn(1.0);
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
    @DisplayName("Campanha: Deve aplicar multiplicador retornado pela Strategy")
    void testDynamicCampaignBonus() {
        // Arrange
        int focusTime = 20;
        LocalDateTime standardDay = standardMonday;

        // Simula uma campanha de 50% bônus (1.5x)
        when(campaignStrategy.calculateMultiplier(any())).thenReturn(1.5);

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.HEALTH, standardDay);

        // Assert (10 base * 1.5 = 15 pontos)
        assertEquals(15, points);
    }

    @Test
    @DisplayName("Acúmulo Dinâmico: Campanha + Bônus de Fim de Semana")
    void testDynamicCampaignOnWeekendBonus() {
        // Arrange
        int focusTime = 20;
        LocalDateTime saturday = LocalDateTime.of(2026, 4, 11, 10, 0); // Fim de semana (+0.20)

        // Simula uma campanha de +100% bônus (2x)
        when(campaignStrategy.calculateMultiplier(any())).thenReturn(2.0);

        // Act
        int points = engine.calculatePoints(focusTime, ActivityCategory.WORK, saturday);

        // Assert (Campanha 2.0 + Fim de semana 0.20 = 2.2 -> 10 * 2.2 = 22 pontos)
        assertEquals(22, points);
    }
}