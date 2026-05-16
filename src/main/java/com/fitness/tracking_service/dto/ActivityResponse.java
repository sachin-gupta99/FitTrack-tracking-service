package com.fitness.tracking_service.dto;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ActivityResponse extends ActivityRequest {

    String id;
//    Integer userId;
//    ActivityType type; // e.g., "Running", "Cycling"
//    Integer duration; // in minutes
    Integer caloriesBurned;
//    LocalDateTime startTime;
//    String metadata; // For any additional info like distance, pace, etc.
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
