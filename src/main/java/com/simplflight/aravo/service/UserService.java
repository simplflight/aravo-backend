package com.simplflight.aravo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.GoogleLoginRequest;
import com.simplflight.aravo.dto.request.UserLoginRequest;
import com.simplflight.aravo.dto.request.UserRegisterRequest;
import com.simplflight.aravo.dto.request.UserUpdateRequest;
import com.simplflight.aravo.dto.response.TokenResponse;
import com.simplflight.aravo.dto.response.UserResponse;
import com.simplflight.aravo.mapper.UserMapper;
import com.simplflight.aravo.repository.ActivityRepository;
import com.simplflight.aravo.repository.InventoryRepository;
import com.simplflight.aravo.repository.UserDailyTrackingRepository;
import com.simplflight.aravo.repository.UserRepository;
import com.simplflight.aravo.security.GoogleTokenValidator;
import com.simplflight.aravo.security.TokenService;
import com.simplflight.aravo.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final UserDailyTrackingRepository trackingRepository;
    private final ActivityRepository activityRepository;

    private final TokenService tokenService;
    
    private final PasswordEncoder passwordEncoder; // Gerenciado pelo Spring Security
    private final MessageUtil messageUtil;
    private final UserMapper userMapper;
    private final GoogleTokenValidator googleTokenValidator;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, messageUtil.get("error.email.in.use"));
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, messageUtil.get("error.nickname.in.use"));
        }

        User user = User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .build();
        
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(UserLoginRequest request) {

        String errorMessage = messageUtil.get("error.invalid.credentials");

        String identifier = request.identifier();
        Optional<User> userOptional;

        if (identifier.contains("@")) {
            userOptional = userRepository.findByEmail(identifier);
        } else {
            userOptional = userRepository.findByNickname(identifier);
        }

        User user = userOptional
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage);
        }

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshToken(String refreshToken) {

        String userEmail = tokenService.validateRefreshToken(refreshToken);

        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, messageUtil.get("error.refresh.token.invalid"));
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, messageUtil.get("error.user.not.found")));

        // Gera novo par de tokens (rotacionamento de Refresh Token)
        String newAccessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = tokenService.generateRefreshToken(user);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public TokenResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            GoogleIdToken idToken = verifyGoogleToken(request.idToken());

            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, messageUtil.get("error.google.token.invalid"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGoogleUser(payload));

            String accessToken = tokenService.generateAccessToken(user);
            String refreshToken = tokenService.generateRefreshToken(user);

            return new TokenResponse(accessToken, refreshToken);
        } catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, messageUtil.get("error.google.auth.failed"));
        }
    }

    private GoogleIdToken verifyGoogleToken(String tokenString) throws Exception {
        return googleTokenValidator.validateToken(tokenString);
    }

    private User createGoogleUser(GoogleIdToken.Payload payload) {

        String name = (String) payload.get("name");

        User newUser = User.builder()
                .email(payload.getEmail())
                .name(name)
                .nickname(generateUniqueNickname(name))
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .points(0)
                .streak(0)
                .build();

        return userRepository.save(newUser);
    }

    private String generateUniqueNickname(String fullName) {

        String baseName = fullName.replaceAll("\\s+", "").toLowerCase();
        String nickname;

        // Verifica se por algum milagre do universo esse nick já existe
        do {
            nickname = baseName + UUID.randomUUID().toString().substring(0, 5);
        } while (userRepository.existsByNickname(nickname));

        return nickname;
    }

    public UserResponse getProfile(User currentUser) {
        return userMapper.toResponse(currentUser);
    }

    @Transactional
    public UserResponse updateProfile(User currentUser, UserUpdateRequest request) {

        if (request.name() != null && !request.name().trim().isEmpty()) {
            currentUser.setName(request.name());
        }

        if (request.nickname() != null && !request.nickname().trim().isEmpty()
            && !request.nickname().equals(currentUser.getNickname())) {
            if (userRepository.existsByNickname(request.nickname())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, messageUtil.get("error.nickname.in.use"));
            }

            currentUser.setNickname(request.nickname());
        }

        User savedUser = userRepository.save(currentUser);

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public void deleteProfile(User currentUser) {

        inventoryRepository.deleteByUser(currentUser);
        trackingRepository.deleteByUser(currentUser);
        activityRepository.deleteByUser(currentUser);

        userRepository.delete(currentUser);
    }
}
