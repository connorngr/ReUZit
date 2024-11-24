package com.connorng.ReUzit.controller;

import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllNonAdminUsers() {
        List<User> users = userService.getAllNonAdminUsers();
        System.out.println(users);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{userId}/toggle-lock")
    public ResponseEntity<User> toggleUserLock(@PathVariable Long userId) {
        User updatedUser = userService.toggleUserLock(userId);
        return ResponseEntity.ok(updatedUser);
    }
}
