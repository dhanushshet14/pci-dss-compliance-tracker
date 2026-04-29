package com.internship.tool.controller;

import com.internship.tool.entity.User;
import com.internship.tool.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    // GET all users
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    // POST create user
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    // DELETE user
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted";
    }

    // UPDATE user
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }
}