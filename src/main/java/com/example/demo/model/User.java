package com.example.demo.model;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

public class User {
    private Long id;
    @NotBlank
    private String username;
    private String password;
    private boolean enabled;
    private Set<Role> roles;
}
