package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Student;
import com.example.demo.model.TaskType;
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

    public TimeEntryService(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
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
        return timeEntryRepository.save(timeEntry);
    }

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
}
