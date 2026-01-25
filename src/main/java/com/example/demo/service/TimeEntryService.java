package com.example.demo.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.TaskType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Student;
import com.example.demo.model.TimeEntry;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TimeEntryRepository;
import com.example.demo.specifications.StudentSpecifications;
import com.example.demo.specifications.TimeEntrySpecifications;

import jakarta.annotation.PostConstruct;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cglib.core.TinyBitSet;
import org.springframework.data.domain.Page;

@Service
@Transactional(readOnly = true)
public class TimeEntryService {
    private final TimeEntryRepository timeEntryRepository;
    private final StudentRepository studentRepository;

    public TimeEntryService(TimeEntryRepository timeEntryRepository, StudentRepository studentRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.studentRepository = studentRepository;
    }

    @PostConstruct
    public void init() {

    }

    @Cacheable(value = "timeEntries", key = "#root.methodName")
    public List<TimeEntry> getAll() {
        return timeEntryRepository.findAll();
    }

    public List<TimeEntry> getByStudentId(Long studentId) {
        return timeEntryRepository.findByStudentId(studentId);
    }

    @Transactional
    @CacheEvict(value = { "timeEntries", "timeEntry" }, allEntries = true)
    public TimeEntry create(TimeEntry timeEntry) {

        Student student = studentRepository.findById(timeEntry.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Студент не найден"));


        timeEntry.setStudent(student);


        return timeEntryRepository.save(timeEntry);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "timeEntry", key = "#id")
    public TimeEntry getById(Long id) {
        return timeEntryRepository.findById(id).orElse(null);
    }

    @Caching(evict = {
            @CacheEvict(value = "timeEntries", allEntries = true),
            @CacheEvict(value = "timeEntry", key = "#id")
    })
    @Transactional
    public TimeEntry update(Long id, TimeEntry timeEntry) {
        return timeEntryRepository.findById(id).map(existingTimeEntry -> {
            existingTimeEntry.setStudent(timeEntry.getStudent());
            existingTimeEntry.setType(timeEntry.getType());
            existingTimeEntry.setDescription(timeEntry.getDescription());
            existingTimeEntry.setStart(timeEntry.getStart());
            existingTimeEntry.setEnd(timeEntry.getEnd());
            existingTimeEntry.setBillable(timeEntry.isBillable());
            return timeEntryRepository.save(existingTimeEntry);
        }).orElse(null);
    }

    @Caching(evict = {
            @CacheEvict(value = "timeEntries", allEntries = true),
            @CacheEvict(value = "timeEntry", key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (timeEntryRepository.existsById(id)) {
            timeEntryRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Page<TimeEntry> getByFilter(Long studentId, TaskType type, Pageable pageable) {
        return timeEntryRepository.findAll(TimeEntrySpecifications.filter(studentId, type), pageable);
    }

    @Transactional
    @CacheEvict(value = { "timeEntries", "timeEntry" }, allEntries = true)
    public TimeEntry start(Long studentId, TaskType type, String description) {
        if (timeEntryRepository.findFirstByStudentIdAndEndIsNull(studentId).isPresent()) {
            throw new IllegalStateException("У студента ID=" + studentId + " уже есть активная запись времени");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент с ID=" + studentId + " не найден"));

        TimeEntry entry = new TimeEntry();
        entry.setStudent(student);
        entry.setType(type);
        entry.setDescription(description != null ? description : "");
        entry.setStart(LocalDateTime.now());
        entry.setEnd(null);
        entry.setBillable(true);

        return timeEntryRepository.save(entry);
    }

    @Transactional
    @CacheEvict(value = { "timeEntries", "timeEntry" }, allEntries = true)
    public TimeEntry stop(Long studentId) {
        TimeEntry activeEntry = timeEntryRepository
                .findFirstByStudentIdAndEndIsNull(studentId)
                .orElseThrow(() -> new IllegalStateException("Нет активной записи для студента ID=" + studentId));

        activeEntry.setEnd(LocalDateTime.now());

        if (activeEntry.getEnd().isBefore(activeEntry.getStart())) {
            throw new IllegalArgumentException("Время окончания не может быть раньше начала");
        }

        return timeEntryRepository.save(activeEntry);

    }

    public List<TimeEntry> getWeeklyEntries(Long studentId) {
        LocalDateTime endWeek = LocalDateTime.now();
        LocalDateTime startWeek = endWeek.minusDays(7);
        return timeEntryRepository.findByStudentIdAndStartBetween(studentId, startWeek, endWeek);
    }
}
