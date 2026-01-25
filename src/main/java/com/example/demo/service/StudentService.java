package com.example.demo.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.StudentReportDto;
import com.example.demo.export.StudentReportsExportWrapper;
import com.example.demo.dto.StudentImportDto;
import com.example.demo.dto.StudentListImportDto;
import com.example.demo.enums.TaskType;
import com.example.demo.model.Student;
import com.example.demo.model.TimeEntry;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TimeEntryRepository;
import com.example.demo.specifications.StudentSpecifications;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final TimeEntryRepository timeEntryRepository;
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final XmlMapper xmlMapper = new XmlMapper();

    @Value("${spring.servlet.multipart.location:uploads}")
    private String uploadLocation;

    public StudentService(StudentRepository studentRepository, TimeEntryRepository timeEntryRepository) {
        this.studentRepository = studentRepository;
        this.timeEntryRepository = timeEntryRepository;
        xmlMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        logger.info("StudentService initialized with upload location: {}", uploadLocation);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students", key = "'allStudents'")
    public List<Student> getAll() {
        try {
            logger.debug("Fetching all students from database");
            List<Student> students = studentRepository.findAll();
            logger.info("Retrieved {} students from database", students.size());
            return students;
        } catch (Exception e) {
            logger.error("Error fetching all students: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<Student> getByName(String name) {
        try {
            logger.debug("Fetching students by name: {}", name);
            List<Student> students = studentRepository.findAllByName(name);
            logger.info("Found {} students with name: {}", students.size(), name);
            return students;
        } catch (Exception e) {
            logger.error("Error fetching students by name {}: {}", name, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = { "students", "student" }, allEntries = true)
    public Student create(Student student) {
        try {
            logger.info("Creating new student: {}", student.getName());
            Student savedStudent = studentRepository.save(student);
            logger.info("Student created with ID: {}, Name: {}", savedStudent.getId(), savedStudent.getName());
            return savedStudent;
        } catch (Exception e) {
            logger.error("Error creating student {}: {}", student.getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "student", key = "#id")
    public Student getById(Long id) {
        try {
            logger.debug("Fetching student by ID: {}", id);
            Student student = studentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Student not found with ID: {}", id);
                        return new RuntimeException("Student not found with ID: " + id);
                    });
            logger.debug("Student found: {} (ID: {})", student.getName(), student.getId());
            return student;
        } catch (Exception e) {
            logger.error("Error fetching student with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "students", allEntries = true),
            @CacheEvict(value = "student", key = "#id")
    })
    @Transactional
    public Student update(Long id, Student student) {
        try {
            logger.info("Updating student with ID: {}", id);
            Student existingStudent = studentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Student not found for update with ID: {}", id);
                        return new RuntimeException("Student not found with ID: " + id);
                    });

            if (student.getName() != null && !student.getName().isBlank()) {
                logger.debug("Updating name for student ID {}: {} -> {}",
                        id, existingStudent.getName(), student.getName());
                existingStudent.setName(student.getName().trim());
            }

            if (student.getGroup() != null) {
                logger.debug("Updating group for student ID {}: {} -> {}",
                        id, existingStudent.getGroup(), student.getGroup());
                existingStudent.setGroup(student.getGroup().trim());
            }

            Student updatedStudent = studentRepository.save(existingStudent);
            logger.info("Student updated successfully: {} (ID: {})", updatedStudent.getName(), updatedStudent.getId());
            return updatedStudent;
        } catch (Exception e) {
            logger.error("Error updating student with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "students", allEntries = true),
            @CacheEvict(value = "student", key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        try {
            logger.info("Deleting student with ID: {}", id);
            if (studentRepository.existsById(id)) {
                logger.debug("Deleting time entries for student ID: {}", id);
                timeEntryRepository.deleteByStudentId(id);

                studentRepository.deleteById(id);
                logger.info("Student deleted successfully: {}", id);
                return true;
            } else {
                logger.warn("Student not found for deletion with ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting student with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<Student> getByFilter(String name, Pageable pageable) {
        try {
            logger.debug("Filtering students with name: {}, page: {}, size: {}",
                    name, pageable.getPageNumber(), pageable.getPageSize());
            Page<Student> result = studentRepository.findAll(StudentSpecifications.filter(name), pageable);
            logger.info("Filtered students: {} records found", result.getTotalElements());
            return result;
        } catch (Exception e) {
            logger.error("Error filtering students with name {}: {}", name, e.getMessage(), e);
            throw e;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("Attempted to upload empty file");
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
            logger.warn("Wrong file format detected: {}", fileName);
            throw new IllegalArgumentException("The system supports only XML files");
        }
    }

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public List<Student> importStudentsFromXmlFile(MultipartFile file) {
        logger.info("Starting student import from XML file: {}", file.getOriginalFilename());

        try {
            validateFile(file);

            // Создаем директорию для импорта, если не существует
            Path importDir = Paths.get(uploadLocation, "imports", "students");
            Files.createDirectories(importDir);
            logger.debug("Import directory created: {}", importDir);

            // Сохраняем файл с timestamp в имени
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String originalFileName = file.getOriginalFilename();
            Path filePath = importDir.resolve(timestamp + "_" + originalFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File saved to: {}", filePath);

            // Чтение и парсинг XML
            try (InputStream fileByteStream = file.getInputStream()) {
                StudentListImportDto importDto = xmlMapper.readValue(fileByteStream,
                        StudentListImportDto.class);
                List<StudentImportDto.StudentDto> studentsToImport = importDto.getStudents();

                logger.info("Found {} students in XML file", studentsToImport.size());

                List<Student> importedStudents = new ArrayList<>();
                int successCount = 0;
                int errorCount = 0;

                for (StudentImportDto.StudentDto studentDto : studentsToImport) {
                    try {
                        List<Student> existingStudents = studentRepository.findAllByName(studentDto.getName());

                        if (existingStudents.isEmpty()) {
                            // Создание нового студента
                            Student newStudent = new Student();
                            newStudent.setName(studentDto.getName());
                            newStudent.setGroup(studentDto.getGroup());

                            Student savedStudent = studentRepository.save(newStudent);
                            importedStudents.add(savedStudent);
                            successCount++;
                            logger.debug("Student imported: {} (ID: {})",
                                    savedStudent.getName(), savedStudent.getId());
                        } else {
                            // Обновление существующего студента
                            Student existingStudent = existingStudents.get(0);
                            existingStudent.setGroup(studentDto.getGroup());

                            Student updatedStudent = studentRepository.save(existingStudent);
                            importedStudents.add(updatedStudent);
                            successCount++;
                            logger.debug("Student updated: {} (ID: {})",
                                    updatedStudent.getName(), updatedStudent.getId());
                        }
                    } catch (Exception e) {
                        errorCount++;
                        logger.error("Error importing student {}: {}",
                                studentDto.getName(), e.getMessage(), e);
                    }
                }

                logger.info("Import completed: {} successful, {} failed, total {}",
                        successCount, errorCount, importedStudents.size());
                return importedStudents;
            }
        } catch (Exception e) {
            logger.error("Error importing students from XML: {}", e.getMessage(), e);
            throw new RuntimeException("Error importing students from XML file", e);
        }
    }

    @Transactional(readOnly = true)
    public StudentReportsExportWrapper exportStudentsToXml() {
        logger.info("Exporting students to XML format");

        try {
            List<Student> students = studentRepository.findAll();
            logger.debug("Retrieved {} students for export", students.size());

            StudentReportsExportWrapper exportWrapper = new StudentReportsExportWrapper();
            exportWrapper.setTotalReports(students.size());

            List<StudentReportDto> reportDtos = students.stream()
                    .map(this::convertToReportDto)
                    .toList();

            exportWrapper.setReports(reportDtos);

            logger.info("Successfully exported {} students to XML wrapper", students.size());
            return exportWrapper;
        } catch (Exception e) {
            logger.error("Error exporting students to XML: {}", e.getMessage(), e);
            throw new RuntimeException("Error exporting students to XML", e);
        }
    }

    private StudentReportDto convertToReportDto(Student student) {
        return new StudentReportDto(
                student.getId(),
                student.getId(),
                student.getName(),
                student.getGroup(),
                TaskType.LAB.name(),
                "Student record",
                LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("dd.MM HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM HH:mm")),
                false,
                0L);
    }

    @Transactional(readOnly = true)
    public byte[] generateStudentsPdfReport() {
        logger.info("Starting students PDF report generation");

        try {
            InputStream templateStream = new ClassPathResource("studentTimeEntriesReport.jrxml").getInputStream();
            logger.debug("PDF template loaded");

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            logger.debug("PDF template compiled");

            List<StudentReportDto> reportData = studentRepository.findAll().stream()
                    .map(student -> {
                        List<TimeEntry> recentEntries = timeEntryRepository
                                .findTop5ByStudent_IdOrderByIdDesc(student.getId());

                        long totalMinutes = recentEntries.stream()
                                .filter(entry -> entry.getStart() != null && entry.getEnd() != null)
                                .mapToLong(entry -> java.time.Duration.between(
                                        entry.getStart(), entry.getEnd()).toMinutes())
                                .sum();

                        String startStr = null;
                        String endStr = null;
                        if (!recentEntries.isEmpty()) {
                            TimeEntry first = recentEntries.get(0);
                            startStr = first.getStart() != null
                                    ? first.getStart().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
                                    : null;
                            endStr = first.getEnd() != null
                                    ? first.getEnd().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
                                    : null;
                        }

                        return new StudentReportDto(
                                student.getId(),
                                student.getId(),
                                student.getName(),
                                student.getGroup(),
                                recentEntries.isEmpty() ? null : recentEntries.get(0).getType().name(),
                                recentEntries.isEmpty() ? "No recent entries"
                                        : "Recent entries: " + recentEntries.size(),
                                startStr,
                                endStr,
                                false,
                                totalMinutes);
                    })
                    .toList();

            logger.debug("Data retrieved from DB and converted to StudentReportDto: {} records",
                    reportData.size());

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            logger.debug("Data source created for PDF generation");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Отчет по студентам");
            parameters.put("generatedBy", "Student Management System");
            parameters.put("generationDate", LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            parameters.put("totalStudents", reportData.size());

            logger.debug("PDF parameters set");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            logger.info("Students PDF report successfully created, size: {} bytes", pdfBytes.length);
            return pdfBytes;
        } catch (Exception e) {
            logger.error("Error creating students PDF report: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating report about students", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateStudentTimeEntriesPdfReport(Long studentId) {
        logger.info("Starting time entries PDF report for student ID: {}", studentId);

        try {
            InputStream templateStream = new ClassPathResource("studentTimeEntriesReport.jrxml").getInputStream();
            logger.debug("Time entries PDF template loaded");

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            logger.debug("Time entries PDF template compiled");

            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> {
                        logger.warn("Student not found for report: {}", studentId);
                        return new RuntimeException("Student not found with ID: " + studentId);
                    });

            logger.debug("Student found for report: {} (ID: {})", student.getName(), student.getId());

            List<TimeEntry> timeEntries = timeEntryRepository.findTop5ByStudent_IdOrderByIdDesc(studentId);
            logger.debug("Retrieved {} time entries for student", timeEntries.size());

            List<StudentReportDto> reportData = timeEntries.stream()
                    .map(entry -> {
                        Long durationMinutes = null;
                        if (entry.getStart() != null && entry.getEnd() != null) {
                            durationMinutes = java.time.Duration.between(entry.getStart(), entry.getEnd()).toMinutes();
                        }

                        String startStr = entry.getStart() != null
                                ? entry.getStart().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
                                : null;
                        String endStr = entry.getEnd() != null
                                ? entry.getEnd().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
                                : null;

                        return new StudentReportDto(
                                entry.getId(),
                                student.getId(),
                                student.getName(),
                                student.getGroup(),
                                entry.getType() != null ? entry.getType().name() : null,
                                entry.getDescription(),
                                startStr,
                                endStr,
                                entry.isBillable(),
                                durationMinutes);
                    })
                    .toList();

            logger.debug("Time entries data converted: {} records", reportData.size());

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Отчет по учету времени студента");
            parameters.put("studentName", student.getName());
            parameters.put("studentGroup", student.getGroup());
            parameters.put("generatedBy", "Student Time Management System");
            parameters.put("generationDate", LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

            long totalMinutes = reportData.stream()
                    .filter(dto -> dto.getDurationMinutes() != null)
                    .mapToLong(StudentReportDto::getDurationMinutes)
                    .sum();
            parameters.put("totalTime", String.format("%d ч %d м",
                    totalMinutes / 60, totalMinutes % 60));

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            logger.info("Time entries PDF report successfully created for student ID: {}, size: {} bytes",
                    studentId, pdfBytes.length);
            return pdfBytes;
        } catch (Exception e) {
            logger.error("Error creating time entries PDF report: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating time entries report", e);
        }
    }
}