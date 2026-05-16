package com.fitness.tracking_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitness.tracking_service.service.ParameterStoreService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties rabbitMQProperties;
    private final ParameterStoreService parameterStoreService;

    @Bean
    public TopicExchange fitnessExchange() {
        return new TopicExchange(rabbitMQProperties.getExchange().getName());
    }

    @Bean
    public Queue activityQueue() {
        return new Queue(rabbitMQProperties.getQueue().getActivityQueue(), true);
    }

    @Bean
    public Queue nutritionQueue() {
        return new Queue(rabbitMQProperties.getQueue().getNutritionQueue(), true);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        String uri = parameterStoreService
                .getParameterValue(rabbitMQProperties.getUri());

        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setUri(uri);
        return cf;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter(objectMapper));
        return rabbitTemplate;
    }

    private MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new MessageConverter() {
            @Override
            @NonNull
            public Message toMessage(@NonNull Object object, @NonNull MessageProperties messageProperties) {
                try {
                    byte[] bytes = objectMapper.writeValueAsBytes(object);
                    messageProperties.setContentType("application/json");
                    return new Message(bytes, messageProperties);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert object to JSON", e);
                }
            }

            @Override
            @NonNull
            public Object fromMessage(@NonNull Message message) {
                throw new UnsupportedOperationException("Use specific conversion in listener");
            }
        };
    }

    @Bean(name = "activityBinding")
    public Binding activityBinding(Queue activityQueue, TopicExchange fitnessExchange) {
        return BindingBuilder
                .bind(activityQueue)
                .to(fitnessExchange)
                .with(rabbitMQProperties.getRoutingKey().getActivityRoutingKey());
    }

    @Bean(name = "nutritionBinding")
    public Binding nutritionBinding(Queue nutritionQueue, TopicExchange fitnessExchange) {
        return BindingBuilder
                .bind(nutritionQueue)
                .to(fitnessExchange)
                .with(rabbitMQProperties.getRoutingKey().getNutritionRoutingKey());
    }
}
