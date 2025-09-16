package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Student;
import com.example.demo.model.TaskType;
import com.example.demo.model.TimeEntry;

@RestController
public class SystemController {
        private List<Student> students = new ArrayList<>(Arrays.asList(
                        new Student(1l, "James", "2231121",
                                        Arrays.asList(new TimeEntry(1l, null, TaskType.CODING, "Programming",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(4), true),
                                                        new TimeEntry(1l, null, TaskType.REST, "Dinner",
                                                                        LocalDateTime.now(),
                                                                        LocalDateTime.now().plusHours(1), false))),
                        new Student(2l, "Kim", "2231122",
                                        Arrays.asList(new TimeEntry(2l, null, TaskType.LECTURE, "Math",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(2), true),
                                                        new TimeEntry(2l, null, TaskType.EXAM_PREP, "Economic",
                                                                        LocalDateTime.now(),
                                                                        LocalDateTime.now().plusHours(2), true))),
                        new Student(3l, "Jack", "2211121",
                                        Arrays.asList(new TimeEntry(3l, null, TaskType.PROJECT, "History",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(3), true),
                                                        new TimeEntry(3l, null, TaskType.REST, "Break",
                                                                        LocalDateTime.now(),
                                                                        LocalDateTime.now().plusMinutes(15), false))),
                        new Student(4l, "Sam", "2231101",
                                        Arrays.asList(new TimeEntry(4l, null, TaskType.LAB, "Data base",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(5), true))),
                        new Student(5l, "Katty", "2251152",
                                        Arrays.asList(new TimeEntry(5l, null, TaskType.CODING,
                                                        "Programming the controller",
                                                        LocalDateTime.now(), LocalDateTime.now().plusHours(6), true),
                                                        new TimeEntry(5l, null, TaskType.REST, "Break",
                                                                        LocalDateTime.now(),
                                                                        LocalDateTime.now().plusMinutes(15), false)))));
}
