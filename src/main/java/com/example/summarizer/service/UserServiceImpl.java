package com.example.summarizer.service;

import com.example.summarizer.exception.ResourceNotFoundException;
import com.example.summarizer.interfaces.UserService;
import com.example.summarizer.model.User;
import com.example.summarizer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        log.info("Fetching user: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    public User createUser(String username, String email, String passwordHash) {
        log.info("Creating user: {}", username);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordHash);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return savedUser;
    }

    public User updateUser(Long id, String email) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmail(email);
        User updated = userRepository.save(user);
        log.info("User updated: {}", id);
        return updated;
    }

    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }
}