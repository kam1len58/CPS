package com.example.demo.model;

import java.security.Permission;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;

public class Role {
    private Long id;
    @NotBlank
    private String title;
    private Set<Permission> permissions;
}
