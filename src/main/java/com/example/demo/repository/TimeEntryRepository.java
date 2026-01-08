package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.enums.TaskType;
import com.example.demo.model.TimeEntry;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long>, JpaSpecificationExecutor<TimeEntry> {
    List<TimeEntry> findByStudentId(Long Id);

    Optional<TimeEntry> findFirstByStudentIdAndEndIsNull(Long studentId);

    List<TimeEntry> findByType(TaskType type);
}