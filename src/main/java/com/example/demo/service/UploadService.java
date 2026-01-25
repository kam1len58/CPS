package com.example.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UploadResponseDto;
import com.example.demo.enums.TaskType;
import com.example.demo.model.Student;
import com.example.demo.model.TimeEntry;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TimeEntryRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private final TimeEntryRepository timeEntryRepository;
    private final StudentRepository studentRepository;

    @Value("${spring.servlet.multipart.location}")
    private String uploadLocation;

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("Attempted to upload empty file");
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            logger.warn("Wrong file format detected: {}", fileName);
            throw new IllegalArgumentException("The system supports only CSV files");
        }
    }

    private Path saveFile(MultipartFile file) throws IOException {
        logger.debug("Saving uploaded file");
        String timestamp = LocalDateTime.now().toString().replaceAll(":", "-");
        String filename = timestamp + "_" + file.getOriginalFilename();

        Path targetLocation = Paths.get(uploadLocation).toAbsolutePath().normalize().resolve(filename);
        logger.debug("File save path: {}", targetLocation);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        logger.info("File saved successfully: {}", filename);
        return targetLocation;
    }

    @Transactional
    public UploadResponseDto importTypeAndTime(MultipartFile file) {
        logger.info("Starting CSV import for time entries");
        validateFile(file);
        int successCount = 0;
        int failureCount = 0;
        LocalDateTime uploadTime = LocalDateTime.now();

        try {
            logger.debug("Saving uploaded CSV file");
            Path savedFile = saveFile(file);
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true).setTrim(true).get();

            logger.debug("Parsing CSV file: {}", savedFile);
            try (BufferedReader reader = Files.newBufferedReader(savedFile, StandardCharsets.UTF_8);
                    CSVParser csvParser = format.parse(reader)) {
                int rowNumber = 1;
                for (CSVRecord record : csvParser) {
                    try {
                        Long studentId = Long.valueOf(record.get("student_id"));
                        logger.debug("Processing row {} - Student ID: {}", rowNumber, studentId);

                        Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Student with id=" + studentId + " not found"));
                        logger.debug("Student found: {} (ID: {})", student.getName(), studentId);

                        String taskType = record.get("task_type");
                        String hours = record.get("estimated_hours");
                        logger.debug("Task type: {}, Estimated hours: {}", taskType, hours);

                        TaskType type = TaskType.valueOf(taskType.toUpperCase());
                        Double time = Double.valueOf(hours);
                        if (time <= 0) {
                            logger.warn("Invalid time value in row {}: {}", rowNumber, time);
                            throw new IllegalArgumentException("Time should be positive");
                        }

                        long totalMinutes = (long) Math.round(time * 60);
                        LocalDateTime start = uploadTime;
                        LocalDateTime end = start.plusMinutes(totalMinutes);
                        logger.debug("Calculated time: {} minutes ({} hours)", totalMinutes, time);

                        var entry = new TimeEntry();
                        entry.setStudent(student);
                        entry.setType(type);
                        entry.setDescription("Normal time: " + taskType);
                        entry.setStart(start);
                        entry.setEnd(end);
                        entry.isBillable();

                        timeEntryRepository.save(entry);
                        successCount++;
                        logger.debug("Time entry saved successfully for student {} in row {}", student.getName(),
                                rowNumber);

                    } catch (Exception e) {
                        failureCount++;
                        logger.warn("Error processing row {}: {}", rowNumber, e.getMessage());
                        System.out.println("Error in line " + rowNumber + ": " + e.getMessage());
                    }
                    rowNumber++;
                }

                logger.info("CSV import completed - Total rows: {}, Success: {}, Failed: {}",
                        rowNumber - 1, successCount, failureCount);
            }
        } catch (IOException e) {
            logger.error("Failed to read CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read CSV file", e);
        } catch (Exception e) {
            logger.error("Error during CSV import: {}", e.getMessage(), e);
            throw e;
        }

        logger.info("CSV import operation completed successfully");
        return new UploadResponseDto(successCount + failureCount, successCount, failureCount);
    }
}