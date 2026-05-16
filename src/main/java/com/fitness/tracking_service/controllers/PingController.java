package com.fitness.tracking_service.controllers;

import com.fitness.tracking_service.dto.GlobalResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/ping")
public class PingController {

    @GetMapping
    public ResponseEntity<GlobalResponseDTO<String>> ping() {
        return ResponseEntity.ok(GlobalResponseDTO.success("pong", "Service is up and running"));
    }
}
