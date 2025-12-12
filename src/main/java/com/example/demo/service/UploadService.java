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
    private final TimeEntryRepository timeEntryRepository;
    private final StudentRepository studentRepository;

    @Value("${spring.servlet.multipart.location}")
    private String uploadLocation;

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("The system supports only CSV files");
        }
    }

    private Path saveFile(MultipartFile file) throws IOException {
        String timestamp = LocalDateTime.now().toString().replaceAll(":", "-");
        String filename = timestamp + "_" + file.getOriginalFilename();

        Path targetLocation = Paths.get(uploadLocation).toAbsolutePath().normalize().resolve(filename);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return targetLocation;
    }

    @Transactional
    public UploadResponseDto importTypeAndTime(MultipartFile file) {
        validateFile(file);
        int successCount = 0;
        int failureCount = 0;
        LocalDateTime uploadTime = LocalDateTime.now();

        try {
            Path savedFile = saveFile(file);
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true).setTrim(true).get();
            try (BufferedReader reader = Files.newBufferedReader(savedFile, StandardCharsets.UTF_8);
                    CSVParser csvParser = format.parse(reader)) {
                int rowNumber = 1;
                for (CSVRecord record : csvParser) {
                    try {
                        Long studentId = Long.valueOf(record.get("student_id"));

                        Student student = studentRepository.findById(studentId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Student with id=" + studentId + " not found"));

                        String taskType = record.get("task_type");
                        String hours = record.get("estimated_hours");

                        TaskType type = TaskType.valueOf(taskType.toUpperCase());
                        Double time = Double.valueOf(hours);
                        if (time <= 0) {
                            throw new IllegalArgumentException("Time should be positive");
                        }

                        long totalMinutes = (long) Math.round(time * 60);
                        LocalDateTime start = uploadTime;
                        LocalDateTime end = start.plusMinutes(totalMinutes);

                        var entry = new TimeEntry();
                        entry.setStudent(student);
                        entry.setType(type);
                        entry.setDescription("Normal time: " + taskType);
                        entry.setStart(start);
                        entry.setEnd(end);
                        entry.isBillable();

                        timeEntryRepository.save(entry);
                        successCount++;
                    } catch (Exception e) {
                        failureCount++;
                        System.out.println("Error in line " + rowNumber + ": " + e.getMessage());
                    }
                    rowNumber++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }
        return new UploadResponseDto(successCount + failureCount, successCount, failureCount);
    }
}
