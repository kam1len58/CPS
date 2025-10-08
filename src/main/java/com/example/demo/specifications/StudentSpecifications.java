package com.example.demo.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.model.Student;

public class StudentSpecifications {
    private static Specification<Student> nameLike(String name) {
        return (root, query, criterialBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criterialBuilder.conjunction();
            }
            return criterialBuilder.like(criterialBuilder.lower(root.get("name")),
                    "%" + name.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Student> filter(String name) {
        return Specification.allOf(nameLike(name));
    }
}
