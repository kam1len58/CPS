package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "studentList")
public class StudentListImportDto {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "student")
    private List<StudentImportDto.StudentDto> students;

    public StudentListImportDto() {
    }

    @XmlElement(name = "students")
    public List<StudentImportDto.StudentDto> getStudents() {
        return students;
    }

    public void setStudents(List<StudentImportDto.StudentDto> students) {
        this.students = students;
    }
}