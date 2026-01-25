package com.example.demo.service;

import java.util.List;

import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.Role;
import com.example.demo.model.User;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional(readOnly = true)
    public List<UserDTO> getUsers() {
        List<UserDTO> userList = userRepository.findAll().stream().map(UserMapper::userToUserDto).toList();
        logger.info("Successfully founded {} user", userList.size());
        return userList;
    }

    @Transactional(readOnly = true)
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            logger.warn("User with id {} not found", id.toString());
            return new ResourceNotFoundException("User with id " + id + "not found");
        });
        logger.info("User with id {} successfully found", id.toString());
        return UserMapper.userToUserDto(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserDTO(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            logger.warn("User with username {} not found", username);
            return new ResourceNotFoundException("User with username " + username + "not found");
        }); // Возврат сущности Дто пользователя с указанным именем. Также есть обработка
            // исключений
        logger.info("User with username {} successfully found", username);
        return UserMapper.userToUserDto(user);
    }

    @Transactional(readOnly = true)
    public User getUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> {
            logger.warn("User with username {} not found", username);
            return new ResourceNotFoundException("User with username " + username + "not found");
        }); // Возврат сущности пользователя с указанным именем. Также есть обработка
            // исключений
        logger.info("User with username {} successfully found", username);
        return user;
    }

    @Transactional
    public User saveUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role 'USER' not found"));
        user.setRole(defaultRole);
        logger.info("User with username {} successfully saved", user.getUsername());
        return userRepository.save(user);
    }
}
