package com.simplflight.aravo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.GoogleLoginRequest;
import com.simplflight.aravo.dto.response.TokenResponse;
import com.simplflight.aravo.repository.UserRepository;
import com.simplflight.aravo.security.GoogleTokenValidator;
import com.simplflight.aravo.security.TokenService;
import com.simplflight.aravo.util.MessageUtil;
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
    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Login Google: Deve retornar JWTs para usuário que já existe no banco")
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

        when(tokenService.generateAccessToken(existingUser)).thenReturn("access.jwt.fake");
        when(tokenService.generateRefreshToken(existingUser)).thenReturn("refresh.jwt.fake");

        // Act
        TokenResponse result = userService.loginWithGoogle(request);

        // Assert
        assertNotNull(result);
        assertEquals("access.jwt.fake", result.accessToken());
        assertEquals("refresh.jwt.fake", result.refreshToken());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Refresh Token: Deve retornar novos tokens se o refresh token enviado for válido")
    void testRefreshToken_Success() {
        // Arrange
        String oldRefreshToken = "old.refresh.token";
        String userEmail = "hero@vesta.com";
        User mockUser = User.builder().email(userEmail).build();

        // TokenService reconhece token antigo e devolve email
        when(tokenService.validateRefreshToken(oldRefreshToken)).thenReturn(userEmail);

        // Usuário encontrado
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        // Gera novos tokens rotacionados
        when(tokenService.generateAccessToken(mockUser)).thenReturn("new.access.token");
        when(tokenService.generateRefreshToken(mockUser)).thenReturn("new.refresh.token");

        // Act
        TokenResponse result = userService.refreshToken(oldRefreshToken);

        // Assert
        assertNotNull(result);
        assertEquals("new.access.token", result.accessToken());
        assertEquals("new.refresh.token", result.refreshToken());
    }

    @Test
    @DisplayName("Refresh Token: Deve lançar erro 401 se o refresh token for inválido, expirado ou for um token de acesso")
    void testRefreshToken_Invalid() {
        // Arrange
        String badToken = "bad.token";

        // TokenService falha na validação e retorna null
        when(tokenService.validateRefreshToken(badToken)).thenReturn(null);

        // Act & Assert
        org.springframework.web.server.ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> userService.refreshToken(badToken)
                );

        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(userRepository, never()).findByEmail(anyString()); // O banco não deve ser consultado
    }
}