package com.internship.tool.service;

import com.internship.tool.entity.User;
import com.internship.tool.exception.UserNotFoundException;
import com.internship.tool.repository.UserRepository;
import com.internship.tool.dto.UserDTO;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ GET all users
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserDTO(user.getName(), user.getEmail()))
                .toList();
    }

    // ✅ PAGINATION
    public Page<UserDTO> getUsersWithPagination(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserDTO(user.getName(), user.getEmail()));
    }

    // ✅ BASIC SEARCH (without pagination)
    public List<UserDTO> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(user -> new UserDTO(user.getName(), user.getEmail()))
                .toList();
    }

    // ✅ 🔥 ADVANCED SEARCH 
    public Page<UserDTO> searchUsers(String name, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(user -> new UserDTO(user.getName(), user.getEmail()));
    }

    // ✅ POST
    public UserDTO saveUser(UserDTO userDTO) {
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());

        User savedUser = userRepository.save(user);

        return new UserDTO(savedUser.getName(), savedUser.getEmail());
    }

    // ✅ DELETE
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ✅ UPDATE
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());

        User updatedUser = userRepository.save(existingUser);

        return new UserDTO(updatedUser.getName(), updatedUser.getEmail());
    }
}