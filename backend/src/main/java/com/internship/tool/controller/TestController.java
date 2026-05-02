package com.internship.tool.controller;

import com.internship.tool.service.UserService;
import com.internship.tool.dto.UserDTO;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    // ✅ GET all users
    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return userService.getAllUsers();
    }

    // ✅ PAGINATION
    @GetMapping("/users/page")
    public Page<UserDTO> getUsersWithPagination(Pageable pageable) {
        return userService.getUsersWithPagination(pageable);
    }

    // ✅ POST
    @PostMapping("/users")
    public UserDTO createUser(@Valid @RequestBody UserDTO userDTO) {
        return userService.saveUser(userDTO);
    }

    // ✅ DELETE
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted";
    }

    // ✅ UPDATE
    @PutMapping("/users/{id}")
    public UserDTO updateUser(@PathVariable Long id,
                             @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    // ✅ 🔥 ADVANCED FILTERING (UPDATED)
    @GetMapping("/users/search")
    public Page<UserDTO> searchUsers(
            @RequestParam String name,
            Pageable pageable
    ) {
        return userService.searchUsers(name, pageable);
    }
}