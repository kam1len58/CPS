package com.example.demo.service;

import java.util.List;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public List<UserDTO> getUsers() {
        return userRepository.findAll().stream().map(UserMapper::userToUserDto).toList();
    }

    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + "not found"));
        return UserMapper.userToUserDto(user);
    }

    public UserDTO getUserDTO(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username " + username + "not found"));
        return UserMapper.userToUserDto(user);
    }

    public User getUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username " + username + "not found"));
        return user;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
