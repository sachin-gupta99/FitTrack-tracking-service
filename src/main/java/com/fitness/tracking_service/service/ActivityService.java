package com.fitness.tracking_service.service;

import com.fitness.tracking_service.config.RabbitMQProperties;
import com.fitness.tracking_service.dto.ActivityRequest;
import com.fitness.tracking_service.dto.ActivityResponse;
import com.fitness.tracking_service.exceptions.RecordNotFoundException;
import com.fitness.tracking_service.model.Activity;
import com.fitness.tracking_service.model.ActivityType;
import com.fitness.tracking_service.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    public ActivityResponse trackActivities(ActivityRequest activityRequest, Integer userId) {

        Boolean isUserValid = userValidationService.validateUser(userId);

        if(isUserValid == null || !isUserValid)
            throw new RecordNotFoundException("User not found with ID: " + userId);

        Activity activity = Activity.builder()
                .userId(activityRequest.getUserId())
                .type(activityRequest.getType())
                .duration(activityRequest.getDuration())
                .caloriesBurned(0)
                .startTime(activityRequest.getStartTime())
                .metadata(activityRequest.getMetadata())
                .build();

        Activity savedActivity = activityRepository.save(activity);

        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange().getName(),
                rabbitMQProperties.getRoutingKey().getActivityRoutingKey(),
                mapToResponse(savedActivity),
                message -> {
                    message.getMessageProperties().setHeader("action", "create");
                    return message;
                }
        );

        return mapToResponse(savedActivity);
    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setMetadata(activity.getMetadata());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }

    public List<ActivityResponse> getUserActivities(Integer userId) {

        List<Activity> activities = activityRepository.findByUserId(userId);
        return activities.stream().map(this::mapToResponse).toList();
    }

    public ActivityResponse getActivityById(String activityId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RecordNotFoundException("Activity not found with ID: " + activityId));
        return mapToResponse(activity);
    }

    public List<String> getAllActivityTypes() {

        // return all types in the enum as a list of strings
        return Stream.of(ActivityType.values())
                .map(ActivityType::name)
                .map(type -> type.charAt(0) + type.substring(1).toLowerCase()) // Capitalize first letter
                .toList();
    }

    public ActivityResponse updateActivity(String activityId, ActivityRequest activityRequest, Integer userId) {

        // Validate that the user exists
        Boolean isUserValid = userValidationService.validateUser(userId);

        if(isUserValid == null || !isUserValid)
            throw new RecordNotFoundException("User not found with ID: " + userId);

        // Find the existing activity
        Activity existingActivity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RecordNotFoundException("Activity not found with ID: " + activityId));

        // Verify that the activity belongs to the user
        if (!existingActivity.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Activity with ID " + activityId + " does not belong to user " + userId);
        }

        // Update the activity fields
        existingActivity.setType(activityRequest.getType());
        existingActivity.setDuration(activityRequest.getDuration());
        existingActivity.setCaloriesBurned(0); // Reset calories burned to 0, it will be recalculated separately
        existingActivity.setStartTime(activityRequest.getStartTime());
        existingActivity.setMetadata(activityRequest.getMetadata());
        // Note: caloriesBurned is not updated from request as it's calculated separately

        // Save the updated activity
        Activity updatedActivity = activityRepository.save(existingActivity);

        // Publish update event to RabbitMQ
        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange().getName(),
                rabbitMQProperties.getRoutingKey().getActivityRoutingKey(),
                mapToResponse(updatedActivity),
                message -> {
                    message.getMessageProperties().setHeader("action", "update");
                    return message;
                }
        );

        return mapToResponse(updatedActivity);
    }

    public void deleteActivity(String activityId, Integer userId) {

        // Validate that the user exists
        Boolean isUserValid = userValidationService.validateUser(userId);

        if(isUserValid == null || !isUserValid)
            throw new RecordNotFoundException("User not found with ID: " + userId);

        // Find the existing activity
        Activity existingActivity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RecordNotFoundException("Activity not found with ID: " + activityId));

        // Verify that the activity belongs to the user
        if (!existingActivity.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Activity with ID " + activityId + " does not belong to user " + userId);
        }

        // Delete the activity
        activityRepository.delete(existingActivity);

        // Publish delete event to RabbitMQ
        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchange().getName(),
                rabbitMQProperties.getRoutingKey().getActivityRoutingKey(),
                new Activity(activityId),
                message -> {
                    message.getMessageProperties().setHeader("action", "delete");
                    return message;
                }
        );
    }
}
