package com.fitness.activity_service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NutritionResponse extends NutritionRequest {

    String id;
    Integer calories;
    Integer protein;
    Integer carbs;
    Integer fat;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
