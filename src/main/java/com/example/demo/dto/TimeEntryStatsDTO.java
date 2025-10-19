package com.example.demo.dto;

import java.util.Map;
import com.example.demo.model.TaskType;

public record TimeEntryStatsDTO(
        Map<TaskType, Integer> entriesCount,
        Long totalTimeMinutes,
        Integer activeStudentsCount) {
}
