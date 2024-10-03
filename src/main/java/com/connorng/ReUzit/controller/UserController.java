package com.connorng.ReUzit.controller;


import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @GetMapping
    public List<User> getAllUsers () {
        return userService.getAllUsers();
    }
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}
