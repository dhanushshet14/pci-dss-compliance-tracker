package com.internship.tool.service;

import com.internship.tool.entity.User;
import com.internship.tool.exception.UserNotFoundException;
import com.internship.tool.repository.UserRepository;
import com.internship.tool.dto.UserDTO;

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private final UserRepository userRepository;

    // 🔥 Logger
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ GET all users
    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(user -> new UserDTO(user.getName(), user.getEmail()))
                .toList();
    }

    // ✅ PAGINATION
    public Page<UserDTO> getUsersWithPagination(Pageable pageable) {
        logger.info("Fetching users with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return userRepository.findAll(pageable)
                .map(user -> new UserDTO(user.getName(), user.getEmail()));
    }

    // ✅ BASIC SEARCH (name only)
    public List<UserDTO> searchUsersByName(String name) {
        logger.info("Searching users by name: {}", name);

        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(user -> new UserDTO(user.getName(), user.getEmail()))
                .toList();
    }

    // ✅ ADVANCED SEARCH (name + email + pagination)
    public Page<UserDTO> searchUsers(String name, String email, Pageable pageable) {
        logger.info("Advanced search - name: {}, email: {}", name, email);

        return userRepository
                .findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(
                        name == null ? "" : name,
                        email == null ? "" : email,
                        pageable
                )
                .map(user -> new UserDTO(user.getName(), user.getEmail()));
    }

    //JPQL SEARCH (Day 7)
    public List<UserDTO> searchUsersJPQL(String name) {
        logger.info("Searching users using JPQL for name: {}", name);

        return userRepository.searchByNameJPQL(name)
                .stream()
                .map(user -> new UserDTO(user.getName(), user.getEmail()))
                .toList();
    }

    // ✅ POST
    public UserDTO saveUser(UserDTO userDTO) {
        logger.info("Saving user: {}", userDTO.getEmail());

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());

        User savedUser = userRepository.save(user);

        logger.info("User saved successfully: {}", savedUser.getEmail());

        return new UserDTO(savedUser.getName(), savedUser.getEmail());
    }

    // ✅ DELETE
    public void deleteUser(Long id) {
        logger.warn("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }

    // ✅ UPDATE
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        logger.info("Updating user with id: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id)
                );

        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());

        User updatedUser = userRepository.save(existingUser);

        logger.info("User updated successfully: {}", updatedUser.getEmail());

        return new UserDTO(updatedUser.getName(), updatedUser.getEmail());
    }
}