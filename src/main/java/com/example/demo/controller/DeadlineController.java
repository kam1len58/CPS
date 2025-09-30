package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.DeadlinePredictionDTO;
import com.example.demo.model.RiskLevel;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class DeadlineController {
    private List<DeadlinePredictionDTO> deadlines = new ArrayList<>(Arrays.asList(
            new DeadlinePredictionDTO(1L, "DB lab report", LocalDateTime.of(2025, 10, 15, 18, 0), 8.0, RiskLevel.HIGH),
            new DeadlinePredictionDTO(2L, "Math homework", LocalDateTime.of(2025, 11, 23, 10, 0), 2.0, RiskLevel.LOW),
            new DeadlinePredictionDTO(3L, "History Research Paper", LocalDateTime.of(2025, 9, 6, 13, 0), 5.0,
                    RiskLevel.MEDIUM),
            new DeadlinePredictionDTO(4L, "Code Refactoring", LocalDateTime.of(2025, 12, 1, 9, 0), 12.0,
                    RiskLevel.HIGH),
            new DeadlinePredictionDTO(5L, "Economic exam prep", LocalDateTime.of(2025, 8, 4, 15, 0), 6.0,
                    RiskLevel.MEDIUM)));

    @GetMapping("/deadline")
    public List<DeadlinePredictionDTO> getDeadlines() {
        return deadlines;
    }

    @PostMapping("/deadline")
    public ResponseEntity<DeadlinePredictionDTO> addDeadline(@RequestBody @Valid DeadlinePredictionDTO deadline) {
        deadline.setId((long) deadlines.size() + 1);
        deadlines.add(deadline);
        return ResponseEntity.status(HttpStatus.CREATED).body(deadline);
    }

    @GetMapping("/deadline/{id}")
    public ResponseEntity<DeadlinePredictionDTO> getDeadLine(@PathVariable Long id) {
        for (DeadlinePredictionDTO deadline : deadlines) {
            if (deadline.getId().equals(id)) {
                return ResponseEntity.ok(deadline);
            }
        }
        return ResponseEntity.notFound().build();
    }
}
