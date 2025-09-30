package com.example.demo.model;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class Role {
    private Long id;
    @NotBlank
    private String title;
    private Set<Permission> permissions;
}
