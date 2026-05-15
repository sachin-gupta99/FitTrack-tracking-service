package com.fitness.activity_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() { return WebClient.builder(); }

    @Bean
    public WebClient userWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .filter((request, next) -> {

                    String token =
                        ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
                            .getRequestAttributes()))
                            .getRequest()
                            .getHeader("Authorization");

                    System.out.println("Forwarding Authorization header: " + token);

                    ClientRequest newRequest = ClientRequest.from(request)
                            .header("Authorization", token)
                            .build();

                    return next.exchange(newRequest);
                })
                .baseUrl("http://user-service:8071") // Base URL for the user service
                .build();
    }
}
