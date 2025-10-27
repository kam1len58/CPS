package com.example.demo.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.enums.TaskType;
import com.example.demo.model.Student;
import com.example.demo.model.TimeEntry;

public class TimeEntrySpecifications {
    public static Specification<TimeEntry> studentIdEquals(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("student").get("id"), studentId);
        };
    }

    public static Specification<TimeEntry> typeEquals(TaskType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<TimeEntry> isBillableEquals(Boolean isBillable) {
        return (root, query, criteriaBuilder) -> {
            if (isBillable == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isBillable"), isBillable);
        };
    }

    public static Specification<TimeEntry> filter(Long studentId, TaskType type) {
        return Specification.allOf(studentIdEquals(studentId), typeEquals(type));
    }
}
