package com.fitness.tracking_service.repository;

import com.fitness.tracking_service.model.Nutrition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionRepository extends MongoRepository<Nutrition, String> {
    List<Nutrition> findByUserId(Integer userId);
}
