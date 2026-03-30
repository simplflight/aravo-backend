package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.UserLoginRequest;
import com.simplflight.aravo.dto.request.UserRegisterRequest;
import com.simplflight.aravo.dto.response.UserResponse;
import com.simplflight.aravo.repository.UserRepository;
import com.simplflight.aravo.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Gerenciado pelo Spring Security
    private final TokenService tokenService;
    private final MessageSource messageSource;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            String errorMessage = messageSource.getMessage("error.email.in.use", null, LocaleContextHolder.getLocale());

            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            String errorMessage = messageSource.getMessage("error.nickname.in.use", null, LocaleContextHolder.getLocale());

            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
        }

        User user = User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .build();
        
        User savedUser = userRepository.save(user);
        
        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public String login(UserLoginRequest request) {
        String errorMessage = messageSource.getMessage("error.invalid.credentials", null, LocaleContextHolder.getLocale());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage);
        }

        return tokenService.generateToken(user);
    }

    public UserResponse getProfile(User currentUser) {
        return mapToResponse(currentUser);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getPoints(),
                user.getTotalPoints(),
                user.getStreak(),
                user.getHighestStreak(),
                user.getCreatedAt(),
                user.getFocusPreference(),
                user.getRestPreference(),
                user.getLastActivityDate()
        );
    }
}
