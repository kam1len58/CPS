package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Student;

import com.example.demo.repository.StudentRepository;
import com.example.demo.specifications.StudentSpecifications;

import jakarta.annotation.PostConstruct;

import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @PostConstruct
    public void init() {

    }

    @Cacheable(value = "students", key = "#root.methodName")
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public List<Student> getByTitle(String name) {
        return studentRepository.findAllByName(name);
    }

    @Transactional
    @CacheEvict(value = { "students", "student" }, allEntries = true)
    public Student create(Student student) {
        return studentRepository.save(student);
    }

    @Cacheable(value = "student", key = "#id")
    public Student getById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Caching(evict = {
            @CacheEvict(value = "students", allEntries = true),
            @CacheEvict(value = { "students", "student" }, key = "#id")
    })
    @Transactional
    public Student update(Long id, Student student) {
        return studentRepository.findById(id).map(existingStudent -> {
            existingStudent.setName(student.getName());
            existingStudent.setGroup(student.getGroup());
            existingStudent.setRecentEntries(student.getRecentEntries());
            return studentRepository.save(existingStudent);
        }).orElse(null);
    }

    @Caching(evict = {
            @CacheEvict(value = "students", allEntries = true),
            @CacheEvict(value = { "students", "student" }, key = "#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Page<Student> getByFilter(String name, Pageable pageable) {
        return studentRepository.findAll(StudentSpecifications.filter(name), pageable);
    }
}
