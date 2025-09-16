package com.example.demo.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class Student {
    private Long id;
    @NotBlank
    private String name;
    private String group;
    private List<TimeEntry> recentEntries;
}
