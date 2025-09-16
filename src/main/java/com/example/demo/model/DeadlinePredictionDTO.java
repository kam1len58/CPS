package com.example.demo.model;

import java.time.LocalDateTime;

public class DeadlinePredictionDTO {
    private String subject;
    private LocalDateTime deadline;
    private Double hoursLeft;
    private RiskLevel risk; // LOW, MEDIUM, HICH
}
