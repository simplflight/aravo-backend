package com.simplflight.aravo.controller;

import com.simplflight.aravo.domain.entity.User;
import com.simplflight.aravo.dto.request.ActivityCompleteRequest;
import com.simplflight.aravo.dto.request.ActivityStartRequest;
import com.simplflight.aravo.dto.response.ActivityResponse;
import com.simplflight.aravo.service.ActivityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Activity", description = "Managing activities and focus sessions.")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping("/start")
    public ResponseEntity<ActivityResponse> start(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ActivityStartRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.startActivity(currentUser, request));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ActivityResponse> complete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ActivityCompleteRequest request
    ) {
        return ResponseEntity.ok(activityService.completeActivity(currentUser, id, request));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@AuthenticationPrincipal User currentUser) {

        List<ActivityResponse> response = activityService.getUserActivities(currentUser);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ActivityCompleteRequest request
    ) {
        return ResponseEntity.ok(activityService.updateActivity(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {

        activityService.deleteActivity(currentUser, id);

        return ResponseEntity.noContent().build();
    }
}