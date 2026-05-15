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
@Document(collection = "nutrition")
public class Nutrition {

    @Id
    String id;
    Integer userId;
    String foodName;
    String mealType; // Breakfast, Lunch, Dinner, Snack
    Integer calories;
    Integer protein;
    Integer carbs;
    Integer fat;
    LocalDateTime loggedAt;
    String servingSize;
    String notes;

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    public Nutrition(String nutritionId) {
        this.id = nutritionId;
    }
}
