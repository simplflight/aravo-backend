package com.simplflight.aravo.controller;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.UserLoginRequest;
import com.simplflight.aravo.dto.request.UserRegisterRequest;
import com.simplflight.aravo.dto.request.UserUpdateRequest;
import com.simplflight.aravo.dto.response.InventoryResponse;
import com.simplflight.aravo.dto.response.StreakCalendarResponse;
import com.simplflight.aravo.dto.response.TokenResponse;
import com.simplflight.aravo.dto.response.UserResponse;
import com.simplflight.aravo.service.InventoryService;
import com.simplflight.aravo.service.StreakService;
import com.simplflight.aravo.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final StreakService streakService;
    private final InventoryService inventoryService;

    @PostMapping("/register")
    @SecurityRequirements() // Documenta a rota como pública
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {

        UserResponse response = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @SecurityRequirements()
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody UserLoginRequest request) {

        String token = userService.login(request);

        return ResponseEntity.ok(new TokenResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal User currentUser) {
        // @AuthenticationPrincipal recupera o usuário injetado no SecurityContext

        UserResponse response = userService.getProfile(currentUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/streak")
    public ResponseEntity<StreakCalendarResponse> getCalendar(
            @AuthenticationPrincipal User currentUser,
            @RequestParam int month,
            @RequestParam int year
    ) {

        StreakCalendarResponse response = streakService.getCalendar(currentUser, month, year);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/inventory")
    public ResponseEntity<List<InventoryResponse>> getMyInventory(@AuthenticationPrincipal User currentUser) {

        List<InventoryResponse> responses = inventoryService.getUserInventory(currentUser);

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserUpdateRequest request
    ) {

        UserResponse response = userService.updateProfile(currentUser, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal User currentUser) {

        userService.deleteProfile(currentUser);

        return ResponseEntity.noContent().build();
    }
}
