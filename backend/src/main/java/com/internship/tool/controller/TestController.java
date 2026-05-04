package com.internship.tool.controller;

import com.internship.tool.service.UserService;
import com.internship.tool.dto.UserDTO;
import com.internship.tool.dto.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    // ✅ GET all users
    @GetMapping("/users")
    public ApiResponse<List<UserDTO>> getUsers() {
        List<UserDTO> users = userService.getAllUsers();

        return new ApiResponse<>(
                "success",
                "Users fetched successfully",
                users
        );
    }

    // ✅ PAGINATION (clean response)
    @GetMapping("/users/page")
    public ApiResponse<Map<String, Object>> getUsersWithPagination(Pageable pageable) {

        Page<UserDTO> result = userService.getUsersWithPagination(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());

        return new ApiResponse<>(
                "success",
                "Users fetched with pagination",
                response
        );
    }

    // ✅ POST
    @PostMapping("/users")
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {

        UserDTO savedUser = userService.saveUser(userDTO);

        return new ApiResponse<>(
                "success",
                "User created successfully",
                savedUser
        );
    }

    // ✅ DELETE
    @DeleteMapping("/users/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return new ApiResponse<>(
                "success",
                "User deleted successfully",
                null
        );
    }

    // ✅ UPDATE
    @PutMapping("/users/{id}")
    public ApiResponse<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {

        UserDTO updatedUser = userService.updateUser(id, userDTO);

        return new ApiResponse<>(
                "success",
                "User updated successfully",
                updatedUser
        );
    }

    // ADVANCED FILTERING (clean response)
    @GetMapping("/users/search")
    public ApiResponse<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            Pageable pageable
    ) {

        Page<UserDTO> result = userService.searchUsers(name, email, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());

        return new ApiResponse<>(
                "success",
                "Users fetched successfully",
                response
        );
    }

    // JPQL SEARCH (Day 7)
    @GetMapping("/users/search/jpql")
    public ApiResponse<List<UserDTO>> searchUsersJPQL(@RequestParam String name) {

        List<UserDTO> users = userService.searchUsersJPQL(name);

        return new ApiResponse<>(
                "success",
                "Users fetched using JPQL",
                users
        );
    }
}