package com.fitness.activity_service.dto;

import com.fitness.activity_service.model.ActivityType;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ActivityRequest {

    Integer userId;
    ActivityType type; // e.g., "Running", "Cycling"
    Integer duration; // in minutes
    LocalDateTime startTime; // ISO format string, e.g., "2024-06-01T10:00:00"
    String metadata; // For any additional info like distance, pace, etc.
}
