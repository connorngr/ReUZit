package com.connorng.ReUzit.controller;


import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}
