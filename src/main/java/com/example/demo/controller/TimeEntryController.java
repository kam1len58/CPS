package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.enums.TaskType;
import com.example.demo.model.Student;
import com.example.demo.model.TimeEntry;
import com.example.demo.service.StudentService;
import com.example.demo.service.TimeEntryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @GetMapping("/timeEntries")
    public List<TimeEntry> getTimeEntries() {
        return timeEntryService.getAll();
    }

    @GetMapping("/timeEntries/{id}")
    public ResponseEntity<TimeEntry> getTimeEntry(@PathVariable Long id) {
        return ResponseEntity.ok().body(timeEntryService.getById(id));
    }

    @PostMapping("/timeEntries")
    public ResponseEntity<TimeEntry> addTimeEntry(@RequestBody @Valid TimeEntry timeEntry) {
        TimeEntry newTimeEntry = timeEntryService.create(timeEntry);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTimeEntry);
    }

    @PutMapping("/timeEntries/{id}")
    public ResponseEntity<TimeEntry> editTimeEntry(@PathVariable Long id, @RequestBody @Valid TimeEntry timeEntry) {
        TimeEntry updated = timeEntryService.update(id, timeEntry);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("timeEntries/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (timeEntryService.deleteById(id)) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/timeEntryFilter")
    public ResponseEntity<Object> getByFilter(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) TaskType type,
            @PageableDefault(page = 0, size = 10, sort = "start") Pageable pageable) {
        return ResponseEntity.ok(timeEntryService.getByFilter(studentId, type, pageable));
    }

    @PostMapping("time/start")
    public ResponseEntity<TimeEntry> startTracking(
            @RequestParam Long studentId,
            @RequestParam TaskType type,
            @RequestParam(required = false) String description) {
        TimeEntry entry = timeEntryService.start(studentId, type, description);
        return ResponseEntity.ok(entry);
    }

    @PostMapping("time/stop")
    public ResponseEntity<TimeEntry> stopTracking(@RequestParam Long studentId) {
        TimeEntry entry = timeEntryService.stop(studentId);
        return ResponseEntity.ok(entry);
    }
}
