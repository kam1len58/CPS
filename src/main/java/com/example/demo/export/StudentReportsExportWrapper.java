package com.example.demo.export;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.dto.StudentReportDto;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "StudentTimeReports")
public class StudentReportsExportWrapper {

    @JacksonXmlProperty(localName = "exportDate", isAttribute = true)
    private String exportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version = "1.0";

    @JacksonXmlProperty(localName = "totalReports", isAttribute = true)
    private int totalReports;

    @JacksonXmlProperty(localName = "system")
    private SystemInfo system = new SystemInfo();

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "report")
    private List<StudentReportDto> reports;

    @Data
    public static class SystemInfo {
        @JacksonXmlProperty(localName = "name")
        private String name = "Student Time Management System";

        @JacksonXmlProperty(localName = "exportedBy")
        private String exportedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        @JacksonXmlProperty(localName = "format")
        private String format = "XML";
    }
}