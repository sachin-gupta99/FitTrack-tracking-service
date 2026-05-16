package com.fitness.tracking_service.dto;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NutritionRequest {

    Integer userId;
    String foodName;
    String mealType; // e.g., breakfast, lunch, dinner, snack
    LocalDateTime loggedAt; // ISO 8601 format, e.g., "2024-06-01T12:00:00Z"
    String servingSize; // e.g., "1 cup", "100g"
    String notes; // Optional notes about the meal
}
