package com.example.demo.dto;

import com.example.demo.enums.TaskType;

import java.util.Map;

public record UpdateStatsDTO(String task,
        Map<TaskType, Integer> usersCount,
        Double globalUpdateRate) {
}
