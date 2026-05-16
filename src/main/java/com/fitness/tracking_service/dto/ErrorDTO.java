package com.fitness.tracking_service.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class ErrorDTO {
    private final String message;
    private final String description;
}
