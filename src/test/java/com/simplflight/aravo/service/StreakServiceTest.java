package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.Inventory;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.domain.entity.UserDailyTracking;
import com.simplflight.aravo.domain.enums.DailyTrackingStatus;
import com.simplflight.aravo.domain.enums.ItemType;
import com.simplflight.aravo.repository.InventoryRepository;
import com.simplflight.aravo.repository.UserDailyTrackingRepository;
import com.simplflight.aravo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock
    private UserDailyTrackingRepository trackingRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StreakService streakService;

    private User testUser;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        
        today = LocalDate.of(2026, 4, 10);

        testUser = User.builder()
                .streak(5)
                .highestStreak(5)
                .lastActivityDate(today.minusDays(1))
                .build();
    }

    @Test
    @DisplayName("Lazy Evaluation: usuário em dia não deve sofrer alterações na ofensiva")
    void testEvaluatePastStreak_UpToDate() {
        // Act
        streakService.evaluatePastStreak(testUser, today);

        // Assert
        assertEquals(5, testUser.getStreak(), "A ofensiva deve ser mantida");
        verify(trackingRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lazy Evaluation: Perdeu 1 dia, SEM itens STREAK_FREEZE, ofensiva é zerada")
    void testEvaluatePastStreak_MissedDay_NoFreeze() {
        // Arrange
        testUser.setLastActivityDate(today.minusDays(2)); // Perdeu o dia de ontem

        // Act
        streakService.evaluatePastStreak(testUser, today);

        // Assert
        assertEquals(0, testUser.getStreak(), "A ofensiva deve ser zerada");
        verify(userRepository, times(1)).save(testUser); // Guarda o usuário zerado
        verify(trackingRepository, never()).save(any()); // Não guarda registos FROZEN
    }

    @Test
    @DisplayName("Lazy Evaluation: Perdeu 1 dia, COM item STREAK_FREEZE, ofensiva mantida")
    void testEvaluatePastStreak_MissedDay_WithFreeze() {
        // Arrange
        testUser.setLastActivityDate(today.minusDays(2)); // Perdeu o dia de ontem

        Inventory mockInventory = new Inventory();
        mockInventory.setQuantity(1); // Tem 1 gelo

        when(inventoryRepository.findByUserAndItem_TypeAndQuantityGreaterThan(
                eq(testUser), eq(ItemType.STREAK_FREEZE), eq(0)))
                .thenReturn(Optional.of(mockInventory));

        // Act
        streakService.evaluatePastStreak(testUser, today);

        // Assert - Estado do Usuário e Inventário
        assertEquals(5, testUser.getStreak(), "A ofensiva deve ser mantida");
        assertEquals(today.minusDays(1), testUser.getLastActivityDate(), "A data de última atividade deve avançar para ontem");
        assertEquals(0, mockInventory.getQuantity(), "O item deve ser consumido");

        // Assert - Comportamento (Verifica se guardou o tracking com estado FROZEN)
        ArgumentCaptor<UserDailyTracking> trackingCaptor = ArgumentCaptor.forClass(UserDailyTracking.class);
        verify(trackingRepository).save(trackingCaptor.capture());

        UserDailyTracking savedTracking = trackingCaptor.getValue();
        assertEquals(DailyTrackingStatus.FROZEN, savedTracking.getStatus());
        assertEquals(today.minusDays(1), savedTracking.getTrackingDate());
    }

    @Test
    @DisplayName("Registo: Primeira atividade do dia deve incrementar ofensiva")
    void testRecordActivityToday_FirstTime() {
        // Arrange
        when(trackingRepository.findByUserAndTrackingDate(testUser, today))
                .thenReturn(Optional.empty()); // Simula que hoje ainda não há tracking criado

        // Act
        streakService.recordActivityToday(testUser, today);

        // Assert
        assertEquals(6, testUser.getStreak());
        assertEquals(6, testUser.getHighestStreak());
        assertEquals(today, testUser.getLastActivityDate());

        verify(userRepository).save(testUser);
        verify(trackingRepository).save(any(UserDailyTracking.class));
    }

    @Test
    @DisplayName("Registo: Segunda atividade do mesmo dia NÃO deve incrementar ofensiva")
    void testRecordActivityToday_SecondTime() {
        // Arrange
        testUser.setLastActivityDate(today); // Simula que o usuário já fez uma atividade hoje

        UserDailyTracking existingTracking = UserDailyTracking.builder()
                .user(testUser)
                .trackingDate(today)
                .activitiesCount(1) // Já tinha 1
                .status(DailyTrackingStatus.COMPLETED)
                .build();

        when(trackingRepository.findByUserAndTrackingDate(testUser, today))
                .thenReturn(Optional.of(existingTracking));

        // Act
        streakService.recordActivityToday(testUser, today);

        // Assert
        assertEquals(5, testUser.getStreak(), "A ofensiva deve continuar 5, pois já tinha sido incrementada");
        assertEquals(2, existingTracking.getActivitiesCount(), "O contador de atividades diárias deve subir");
        verify(userRepository, never()).save(testUser); // Como não é o primeiro foco, não deve guardar o usuário
    }
}