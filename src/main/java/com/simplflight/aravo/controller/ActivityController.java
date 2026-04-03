package com.simplflight.aravo.controller;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.ActivityRegisterRequest;
import com.simplflight.aravo.dto.response.ActivityResponse;
import com.simplflight.aravo.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> registerActivity(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ActivityRegisterRequest request
    ) {

        ActivityResponse response = activityService.registerActivity(user, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@AuthenticationPrincipal User user) {

        List<ActivityResponse> response = activityService.getUserActivities(user);

        return ResponseEntity.ok(response);
    }
}