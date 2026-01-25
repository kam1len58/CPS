package com.example.demo.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

@Service
@Transactional(readOnly = true)
public class TimeEntryService {
    private static final Logger logger = LoggerFactory.getLogger(TimeEntryService.class);

    private final TimeEntryRepository timeEntryRepository;
    private final StudentRepository studentRepository;

    public TimeEntryService(TimeEntryRepository timeEntryRepository, StudentRepository studentRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.studentRepository = studentRepository;
    }

    @PostConstruct
    public void init() {
        logger.info("TimeEntryService initialized");
    }

    @Cacheable(value = "timeEntries", key = "#root.methodName")
    public List<TimeEntry> getAll() {
        try {
            logger.debug("Fetching all time entries from database");
            List<TimeEntry> entries = timeEntryRepository.findAll();
            logger.info("Retrieved {} time entries from database", entries.size());
            return entries;
        } catch (Exception e) {
            logger.error("Error fetching all time entries: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<TimeEntry> getByStudentId(Long studentId) {
        try {
            logger.debug("Fetching time entries for student ID: {}", studentId);
            List<TimeEntry> entries = timeEntryRepository.findByStudentId(studentId);
            logger.info("Retrieved {} time entries for student ID: {}", entries.size(), studentId);
            return entries;
        } catch (Exception e) {
            logger.error("Error fetching time entries for student ID {}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = { "timeEntries", "timeEntry" }, allEntries = true)
    public TimeEntry create(TimeEntry timeEntry) {
        try {
            logger.info("Creating new time entry for student ID: {}", timeEntry.getStudent().getId());

            Student student = studentRepository.findById(timeEntry.getStudent().getId())
                    .orElseThrow(() -> {
                        logger.warn("Student not found for time entry creation: {}", timeEntry.getStudent().getId());
                        return new ResourceNotFoundException("Студент не найден");
                    });

            logger.debug("Student found: {} (ID: {})", student.getName(), student.getId());
            timeEntry.setStudent(student);

            TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
            logger.info("Time entry created with ID: {} for student: {}", savedEntry.getId(), student.getName());
            return savedEntry;
        } catch (Exception e) {
            logger.error("Error creating time entry: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "timeEntry", key = "#id")
    public TimeEntry getById(Long id) {
        try {
            logger.debug("Fetching time entry by ID: {}", id);
            TimeEntry entry = timeEntryRepository.findById(id).orElse(null);
            if (entry != null) {
                logger.debug("Time entry found: ID: {}, Student: {}", id, entry.getStudent().getName());
            } else {
                logger.warn("Time entry not found with ID: {}", id);
            }
            return entry;
        } catch (Exception e) {
            logger.error("Error fetching time entry with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "timeEntries", allEntries = true),
            @CacheEvict(value = "timeEntry", key = "#id")
    })
    @Transactional
    public TimeEntry update(Long id, TimeEntry timeEntry) {
        try {
            logger.info("Updating time entry with ID: {}", id);
            return timeEntryRepository.findById(id).map(existingTimeEntry -> {
                logger.debug("Updating fields for time entry ID: {}", id);
                existingTimeEntry.setStudent(timeEntry.getStudent());
                existingTimeEntry.setType(timeEntry.getType());
                existingTimeEntry.setDescription(timeEntry.getDescription());
                existingTimeEntry.setStart(timeEntry.getStart());
                existingTimeEntry.setEnd(timeEntry.getEnd());
                existingTimeEntry.setBillable(timeEntry.isBillable());

                TimeEntry updatedEntry = timeEntryRepository.save(existingTimeEntry);
                logger.info("Time entry updated successfully: ID: {}", id);
                return updatedEntry;
            }).orElse(null);
        } catch (Exception e) {
            logger.error("Error updating time entry with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "timeEntries", allEntries = true),
            @CacheEvict(value = "timeEntry", key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        try {
            logger.info("Deleting time entry with ID: {}", id);
            if (timeEntryRepository.existsById(id)) {
                timeEntryRepository.deleteById(id);
                logger.info("Time entry deleted successfully: ID: {}", id);
                return true;
            } else {
                logger.warn("Time entry not found for deletion: ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting time entry with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public Page<TimeEntry> getByFilter(Long studentId, TaskType type, Pageable pageable) {
        try {
            logger.debug("Filtering time entries - Student ID: {}, Type: {}, Page: {}, Size: {}",
                    studentId, type, pageable.getPageNumber(), pageable.getPageSize());
            Page<TimeEntry> result = timeEntryRepository.findAll(TimeEntrySpecifications.filter(studentId, type),
                    pageable);
            logger.info("Filtered time entries: {} records found", result.getTotalElements());
            return result;
        } catch (Exception e) {
            logger.error("Error filtering time entries: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = { "timeEntries", "timeEntry" }, allEntries = true)
    public TimeEntry start(Long studentId, TaskType type, String description) {
        try {
            logger.info("Starting time tracking for student ID: {}, Type: {}", studentId, type);

            if (timeEntryRepository.findFirstByStudentIdAndEndIsNull(studentId).isPresent()) {
                logger.warn("Student ID: {} already has an active time entry", studentId);
                throw new IllegalStateException("У студента ID=" + studentId + " уже есть активная запись времени");
            }

            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> {
                        logger.warn("Student not found for time tracking start: {}", studentId);
                        return new IllegalArgumentException("Студент с ID=" + studentId + " не найден");
                    });

            logger.debug("Student found: {} (ID: {})", student.getName(), studentId);

            TimeEntry entry = new TimeEntry();
            entry.setStudent(student);
            entry.setType(type);
            entry.setDescription(description != null ? description : "");
            entry.setStart(LocalDateTime.now());
            entry.setEnd(null);
            entry.setBillable(true);

            TimeEntry savedEntry = timeEntryRepository.save(entry);
            logger.info("Time tracking started: Entry ID: {}, Student: {}, Start time: {}",
                    savedEntry.getId(), student.getName(), savedEntry.getStart());
            return savedEntry;
        } catch (Exception e) {
            logger.error("Error starting time tracking for student ID {}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = { "timeEntries", "timeEntry" }, allEntries = true)
    public TimeEntry stop(Long studentId) {
        try {
            logger.info("Stopping time tracking for student ID: {}", studentId);

            TimeEntry activeEntry = timeEntryRepository
                    .findFirstByStudentIdAndEndIsNull(studentId)
                    .orElseThrow(() -> {
                        logger.warn("No active time entry found for student ID: {}", studentId);
                        return new IllegalStateException("Нет активной записи для студента ID=" + studentId);
                    });

            logger.debug("Active entry found: ID: {}, Start time: {}",
                    activeEntry.getId(), activeEntry.getStart());

            activeEntry.setEnd(LocalDateTime.now());

            if (activeEntry.getEnd().isBefore(activeEntry.getStart())) {
                logger.warn("End time {} is before start time {} for entry ID: {}",
                        activeEntry.getEnd(), activeEntry.getStart(), activeEntry.getId());
                throw new IllegalArgumentException("Время окончания не может быть раньше начала");
            }

            TimeEntry stoppedEntry = timeEntryRepository.save(activeEntry);
            logger.info("Time tracking stopped: Entry ID: {}, Duration: {} minutes",
                    stoppedEntry.getId(),
                    java.time.Duration.between(stoppedEntry.getStart(), stoppedEntry.getEnd()).toMinutes());

            return stoppedEntry;
        } catch (Exception e) {
            logger.error("Error stopping time tracking for student ID {}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }

    public List<TimeEntry> getWeeklyEntries(Long studentId) {
        try {
            logger.debug("Fetching weekly time entries for student ID: {}", studentId);

            LocalDateTime endWeek = LocalDateTime.now();
            LocalDateTime startWeek = endWeek.minusDays(7);

            List<TimeEntry> entries = timeEntryRepository.findByStudentIdAndStartBetween(studentId, startWeek, endWeek);
            logger.info("Retrieved {} weekly time entries for student ID: {}", entries.size(), studentId);

            return entries;
        } catch (Exception e) {
            logger.error("Error fetching weekly time entries for student ID {}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }
}