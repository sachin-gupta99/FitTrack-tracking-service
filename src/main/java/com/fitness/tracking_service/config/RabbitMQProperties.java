package com.fitness.tracking_service.config;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("rabbitmq")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RabbitMQProperties {

    String uri;
    RabbitMQQueue queue = new RabbitMQQueue();
    RabbitMQExchange exchange = new RabbitMQExchange();
    RabbitMQRoutingKey routingKey = new RabbitMQRoutingKey();

    @Data
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class RabbitMQQueue {

        String activityQueue;
        String nutritionQueue;
    }

    @Data
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class RabbitMQExchange {

        String name;
        String topic;
    }

    @Data
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class RabbitMQRoutingKey {

        String activityRoutingKey;
        String nutritionRoutingKey;
    }
}
