package com.connorng.ReUzit.controller.User;


import com.connorng.ReUzit.exception.ResourceNotFoundException;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public User createUser(@ModelAttribute User user) {
        return userService.createUser(user);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        // Return the user details that are already authenticated and extracted from the JWT
        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser() {
        String email = userService.getCurrentUserEmail();
        return ResponseEntity.ok(userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))); // Thay thế ResourceNotFoundException với exception phù hợp
    }

}
