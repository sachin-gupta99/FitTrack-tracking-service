package com.fitness.activity_service.controllers;

import com.fitness.activity_service.dto.GlobalResponseDTO;
import com.fitness.activity_service.dto.NutritionRequest;
import com.fitness.activity_service.dto.NutritionResponse;
import com.fitness.activity_service.service.NutritionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/nutrition")
public class NutritionController {

    private final NutritionService nutritionService;

    @GetMapping
    public ResponseEntity<GlobalResponseDTO<List<String>>> getAllNutritionTypes() {
        return ResponseEntity.ok(GlobalResponseDTO.success(nutritionService.getAllNutritionTypes(), "All nutrition types retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GlobalResponseDTO<List<NutritionResponse>>> getUserNutrition(@PathVariable Integer userId) {
        return ResponseEntity.ok(GlobalResponseDTO.success(nutritionService.getUserNutrition(userId), "User nutrition records retrieved successfully"));
    }

    @GetMapping("/{nutritionId}")
    public ResponseEntity<GlobalResponseDTO<NutritionResponse>> getNutritionById(@PathVariable String nutritionId) {
        return ResponseEntity.ok(GlobalResponseDTO.success(nutritionService.getNutritionById(nutritionId), "Nutrition record retrieved successfully"));
    }

    @PostMapping("/user/{userId}/log")
    public ResponseEntity<GlobalResponseDTO<NutritionResponse>> logNutrition(@RequestBody NutritionRequest nutritionRequest, @PathVariable Integer userId) {
        return ResponseEntity.ok(GlobalResponseDTO.success(nutritionService.logNutrition(nutritionRequest, userId), "Nutrition logged successfully"));
    }

    @PutMapping("/{nutritionId}/user/{userId}")
    public ResponseEntity<GlobalResponseDTO<NutritionResponse>> updateNutrition(
            @PathVariable String nutritionId,
            @PathVariable Integer userId,
            @RequestBody NutritionRequest nutritionRequest) {
        return ResponseEntity.ok(GlobalResponseDTO.success(nutritionService.updateNutrition(nutritionId, nutritionRequest, userId), "Nutrition record updated successfully"));
    }

    @DeleteMapping("/{nutritionId}/user/{userId}")
    public ResponseEntity<GlobalResponseDTO<String>> deleteNutrition(@PathVariable String nutritionId, @PathVariable Integer userId) {
        nutritionService.deleteNutrition(nutritionId, userId);
        return ResponseEntity.ok(GlobalResponseDTO.success("Nutrition record deleted successfully", "Nutrition record deleted successfully"));
    }
}
