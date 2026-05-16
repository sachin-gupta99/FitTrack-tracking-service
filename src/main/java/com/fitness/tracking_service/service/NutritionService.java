package com.fitness.tracking_service.service;

import com.fitness.tracking_service.config.RabbitMQConfig;
import com.fitness.tracking_service.dto.NutritionRequest;
import com.fitness.tracking_service.dto.NutritionResponse;
import com.fitness.tracking_service.exceptions.RecordNotFoundException;
import com.fitness.tracking_service.model.Nutrition;
import com.fitness.tracking_service.model.NutritionType;
import com.fitness.tracking_service.repository.NutritionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final NutritionRepository nutritionRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    public NutritionResponse logNutrition(NutritionRequest nutritionRequest, Integer userId) {

        Boolean isUserValid = userValidationService.validateUser(userId);

        if (isUserValid == null || !isUserValid)
            throw new RecordNotFoundException("User not found with ID: " + userId);

        Nutrition nutrition = Nutrition.builder()
                .userId(nutritionRequest.getUserId())
                .foodName(nutritionRequest.getFoodName())
                .mealType(nutritionRequest.getMealType())
                .calories(0)
                .protein(0)
                .carbs(0)
                .fat(0)
                .loggedAt(nutritionRequest.getLoggedAt())
                .servingSize(nutritionRequest.getServingSize())
                .notes(nutritionRequest.getNotes())
                .build();

        Nutrition savedNutrition = nutritionRepository.save(nutrition);

        // Publishing to RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.NUTRITION_ROUTING_KEY,
                mapToResponse(savedNutrition),
                message -> {
                    message.getMessageProperties().setHeader("action", "create");
                    return message;
                }
        );

        return mapToResponse(savedNutrition);
    }

    private NutritionResponse mapToResponse(Nutrition nutrition) {
        NutritionResponse response = new NutritionResponse();
        response.setId(nutrition.getId());
        response.setUserId(nutrition.getUserId());
        response.setFoodName(nutrition.getFoodName());
        response.setMealType(nutrition.getMealType());
        response.setCalories(nutrition.getCalories());
        response.setProtein(nutrition.getProtein());
        response.setCarbs(nutrition.getCarbs());
        response.setFat(nutrition.getFat());
        response.setLoggedAt(nutrition.getLoggedAt());
        response.setServingSize(nutrition.getServingSize());
        response.setNotes(nutrition.getNotes());
        response.setCreatedAt(nutrition.getCreatedAt());
        response.setUpdatedAt(nutrition.getUpdatedAt());
        return response;
    }

    public List<NutritionResponse> getUserNutrition(Integer userId) {

        List<Nutrition> nutritionList = nutritionRepository.findByUserId(userId);
        return nutritionList.stream().map(this::mapToResponse).toList();
    }

    public NutritionResponse getNutritionById(String nutritionId) {

        Nutrition nutrition = nutritionRepository.findById(nutritionId)
                .orElseThrow(() -> new RecordNotFoundException("Nutrition record not found with ID: " + nutritionId));
        return mapToResponse(nutrition);
    }

    public List<String> getAllNutritionTypes() {

        // return all types in the enum as a list of strings
        return Stream.of(NutritionType.values())
                .map(NutritionType::name)
                .map(type -> type.charAt(0) + type.substring(1).toLowerCase().replace("_", " ")) // Capitalize first letter
                .toList();
    }

    public NutritionResponse updateNutrition(String nutritionId, NutritionRequest nutritionRequest, Integer userId) {

        // Validate that the user exists
        Boolean isUserValid = userValidationService.validateUser(userId);

        if (isUserValid == null || !isUserValid)
            throw new RecordNotFoundException("User not found with ID: " + userId);

        // Find the existing nutrition record
        Nutrition existingNutrition = nutritionRepository.findById(nutritionId)
                .orElseThrow(() -> new RecordNotFoundException("Nutrition record not found with ID: " + nutritionId));

        // Verify that the nutrition record belongs to the user
        if (!existingNutrition.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Nutrition record with ID " + nutritionId + " does not belong to user " + userId);
        }

        // Update the nutrition fields
        existingNutrition.setFoodName(nutritionRequest.getFoodName());
        existingNutrition.setMealType(nutritionRequest.getMealType());
        existingNutrition.setCalories(0); // Reset calories, will be recalculated separately
        existingNutrition.setProtein(0);
        existingNutrition.setCarbs(0);
        existingNutrition.setFat(0);
        existingNutrition.setLoggedAt(nutritionRequest.getLoggedAt());
        existingNutrition.setServingSize(nutritionRequest.getServingSize());
        existingNutrition.setNotes(nutritionRequest.getNotes());

        // Save the updated nutrition record
        Nutrition updatedNutrition = nutritionRepository.save(existingNutrition);

        // Publish update event to RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.NUTRITION_ROUTING_KEY,
                mapToResponse(updatedNutrition),
                message -> {
                    message.getMessageProperties().setHeader("action", "update");
                    return message;
                }
        );

        return mapToResponse(updatedNutrition);
    }

    public void deleteNutrition(String nutritionId, Integer userId) {

        // Validate that the user exists
        Boolean isUserValid = userValidationService.validateUser(userId);

        if (isUserValid == null || !isUserValid)
            throw new RecordNotFoundException("User not found with ID: " + userId);

        // Find the existing nutrition record
        Nutrition existingNutrition = nutritionRepository.findById(nutritionId)
                .orElseThrow(() -> new RecordNotFoundException("Nutrition record not found with ID: " + nutritionId));

        // Verify that the nutrition record belongs to the user
        if (!existingNutrition.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Nutrition record with ID " + nutritionId + " does not belong to user " + userId);
        }

        // Delete the nutrition record
        nutritionRepository.delete(existingNutrition);

        // Publish delete event to RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.NUTRITION_ROUTING_KEY,
                new Nutrition(nutritionId),
                message -> {
                    message.getMessageProperties().setHeader("action", "delete");
                    return message;
                }
        );
    }
}

