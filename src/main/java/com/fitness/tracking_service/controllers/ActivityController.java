package com.fitness.tracking_service.controllers;

import com.fitness.tracking_service.dto.ActivityRequest;
import com.fitness.tracking_service.dto.ActivityResponse;
import com.fitness.tracking_service.dto.GlobalResponseDTO;
import com.fitness.tracking_service.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<GlobalResponseDTO<List<String>>> getAllActivityTypes() {
        return ResponseEntity.ok(GlobalResponseDTO.success(activityService.getAllActivityTypes(), "All activity types retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GlobalResponseDTO<List<ActivityResponse>>> getUserActivities(@PathVariable Integer userId) {
        return ResponseEntity.ok(GlobalResponseDTO.success(activityService.getUserActivities(userId), "User activities retrieved successfully"));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<GlobalResponseDTO<ActivityResponse>> getActivityById(@PathVariable String activityId) {
        return ResponseEntity.ok(GlobalResponseDTO.success(activityService.getActivityById(activityId), "Activity retrieved successfully"));
    }

    @PostMapping("/user/{userId}/track")
    public ResponseEntity<GlobalResponseDTO<ActivityResponse>> trackActivity(@RequestBody ActivityRequest activityRequest, @PathVariable Integer userId) {
        return ResponseEntity.ok(GlobalResponseDTO.success(activityService.trackActivities(activityRequest, userId), "Activities tracked successfully"));
    }

    @PutMapping("/{activityId}/user/{userId}")
    public ResponseEntity<GlobalResponseDTO<ActivityResponse>> updateActivity(
            @PathVariable String activityId,
            @PathVariable Integer userId,
            @RequestBody ActivityRequest activityRequest) {
        return ResponseEntity.ok(GlobalResponseDTO.success(activityService.updateActivity(activityId, activityRequest, userId), "Activity updated successfully"));
    }

    @DeleteMapping("/{activityId}/user/{userId}")
    public ResponseEntity<GlobalResponseDTO<String>> deleteActivity(@PathVariable String activityId, @PathVariable Integer userId) {
        activityService.deleteActivity(activityId, userId);
        return ResponseEntity.ok(GlobalResponseDTO.success("Activity deleted successfully", "Activity deleted successfully"));
    }
}
