package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.event.UserLeveledUpEvent;
import com.simplflight.aravo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserLevelServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserLevelService userLevelService;

    @Test
    @DisplayName("Deve distribuir 20 Fragmentos de Foco multiplicados pelo nível alcançado")
    void testHandleUserLeveledUp() {
        // Arrange
        User testUser = User.builder()
                .shards(50) // O usuário já tem 50 Fragmentos na carteira
                .level(3)   // Nível 3
                .build();

        UserLeveledUpEvent event = new UserLeveledUpEvent(testUser);

        // Act
        userLevelService.handleUserLeveledUp(event);

        // Assert
        // O prêmio é Nível (3) * 20 = 60 shards.
        // A carteira tem que ficar com 110 shards (50 + 60).
        assertEquals(110, testUser.getShards(), "O usuário deve ter recebido a quantidade correta de Shards");

        verify(userRepository, times(1)).save(testUser);
    }
}