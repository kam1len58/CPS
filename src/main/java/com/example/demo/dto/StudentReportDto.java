package com.example.demo.dto;

import java.time.LocalDateTime;

public class StudentReportDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentGroup;
    private String type;
    private String description;
    private String start;
    private String end;
    private Boolean billable;
    private Long durationMinutes;

    public StudentReportDto(Long id, Long studentId, String studentName, String studentGroup,
            String type, String description, String start,
            String end, Boolean billable, Long durationMinutes) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentGroup = studentGroup;
        this.type = type;
        this.description = description;
        this.start = start;
        this.end = end;
        this.billable = billable;
        this.durationMinutes = durationMinutes;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentGroup() {
        return studentGroup;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public Boolean getBillable() {
        return billable;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }
}