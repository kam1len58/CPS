package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.TimeEntry;
import com.example.demo.model.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {
    private List<User> users = new ArrayList<>(Arrays.asList(
            new User(1L, "Mickey", "MICKEY2005", true,
                    Set.of(new Role(1L, "STUDENT", Set.of(new Permission(1l, "READ"), new Permission(2l,
                            "CREATE"))))),
            new User(2L, "Kendal", "0091LADNEK", false,
                    Set.of(new Role(2L, "TEACHER", Set.of(new Permission(1l, "READ"), new Permission(2l, "CREATE"),
                            new Permission(3l, "CHECKED_STUDENT"), new Permission(4l, "SENT_COMMENTS_STUDENT"))))),
            new User(3L, "SARA", "S23A24R25A26", false,
                    Set.of(new Role(3L, "ADMIN", Set.of(new Permission(1l, "READ_ALL"), new Permission(2l, "CREATE"),
                            new Permission(3l, "CHECKED_ALL"), new Permission(4l, "DELETE_USER"),
                            new Permission(5l, "ADD_USER"), new Permission(6l, "UPPDATE_SYSTEM")))))));

    @GetMapping("/user")
    public List<User> getUsers() {
        return users;
    }

    @PostMapping("/user")
    public ResponseEntity<User> addUser(@RequestBody @Valid User user) {
        user.setId((long) users.size() + 1);// TODO: process POST request
        users.add(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getStudent(@PathVariable Long id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.notFound().build();
    }
}
