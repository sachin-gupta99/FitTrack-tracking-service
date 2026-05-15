package com.fitness.activity_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Document(collection = "activities")
public class Activity {

    @Id
    String id;
    Integer userId;
    ActivityType type; // e.g., "Running", "Cycling"
    Integer duration; // in minutes
    Integer caloriesBurned;
    LocalDateTime startTime;
    String metadata; // For any additional info like distance, pace, etc.

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    public Activity(String activityId) {
        this.id = activityId;
    }
}
