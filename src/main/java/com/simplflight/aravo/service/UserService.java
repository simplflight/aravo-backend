package com.simplflight.aravo.service;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.UserRegisterRequest;
import com.simplflight.aravo.dto.response.UserResponse;
import com.simplflight.aravo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Gerenciado pelo Spring Security

    @Transactional
    public UserResponse register(UserRegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este email já está em uso.");
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este nickname já está em uso.");
        }

        User user = User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .registrationYear(LocalDate.now().getYear())
                .build();
        
        User savedUser = userRepository.save(user);
        
        return mapToResponse(savedUser);
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
                user.getRegistrationYear(),
                user.getFocusPreference(),
                user.getRestPreference(),
                user.getLastActivityDate()
        );
    }
}
