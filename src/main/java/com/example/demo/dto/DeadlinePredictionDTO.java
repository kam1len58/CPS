package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.model.RiskLevel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class DeadlinePredictionDTO {
    private Long id;
    @NotBlank
    private String subject;
    private LocalDateTime deadline;
    private Double hoursLeft;
    private RiskLevel risk; // LOW, MEDIUM, HICH
}
