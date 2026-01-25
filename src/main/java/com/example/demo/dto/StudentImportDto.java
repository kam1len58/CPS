package com.example.demo.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import java.util.List;

@Data
@JacksonXmlRootElement(localName = "studentsExport")
public class StudentImportDto {

    @JacksonXmlProperty(localName = "exportDate", isAttribute = true)
    private String exportDate;

    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version;

    @JacksonXmlProperty(localName = "totalCount", isAttribute = true)
    private int totalCount;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "student")
    private List<StudentDto> students;

    @Data
    public static class StudentDto {
        @JacksonXmlProperty(localName = "id", isAttribute = true)
        private Long id;

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "group")
        private String group;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "timeEntry")
        private List<TimeEntryDto> recentEntries;
    }

    @Data
    public static class TimeEntryDto {
        @JacksonXmlProperty(localName = "id", isAttribute = true)
        private Long id;

        @JacksonXmlProperty(localName = "type")
        private String type;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "start")
        private String start;

        @JacksonXmlProperty(localName = "end")
        private String end;

        @JacksonXmlProperty(localName = "billable")
        private boolean billable;
    }
}