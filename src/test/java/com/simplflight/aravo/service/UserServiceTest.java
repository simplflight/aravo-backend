package com.simplflight.aravo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.GoogleLoginRequest;
import com.simplflight.aravo.repository.UserRepository;
import com.simplflight.aravo.security.GoogleTokenValidator;
import com.simplflight.aravo.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private GoogleTokenValidator googleTokenValidator;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Login Google: Deve retornar JWT para usuário que já existe no banco")
    void testGoogleLogin_ExistingUser() throws Exception {
        // Arrange
        String mockTokenString = "token.fake.do.google";
        GoogleLoginRequest request = new GoogleLoginRequest(mockTokenString);

        // Mock de GoogleIdToken e Payload
        GoogleIdToken mockGoogleToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = new GoogleIdToken.Payload();
        mockPayload.setEmail("teste@google.com");

        when(mockGoogleToken.getPayload()).thenReturn(mockPayload);

        // Facade retorna o token mockado
        when(googleTokenValidator.validateToken(mockTokenString)).thenReturn(mockGoogleToken);

        User existingUser = User.builder().email("teste@google.com").build();
        when(userRepository.findByEmail("teste@google.com")).thenReturn(Optional.of(existingUser));

        when(tokenService.generateToken(existingUser)).thenReturn("jwt.token.fake");

        // Act
        String result = userService.loginWithGoogle(request);

        // Assert
        assertNotNull(result);
        assertEquals("jwt.token.fake", result);
        verify(userRepository, never()).save(any(User.class)); // Garante que não recriou o usuário
    }
}