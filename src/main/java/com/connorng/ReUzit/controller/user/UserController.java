package com.connorng.ReUzit.controller.user;


import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
//        String email = userService.getCurrentUserEmail();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))); // Thay thế ResourceNotFoundException với exception phù hợp
    }

    @PutMapping("/{id}/money")
    public ResponseEntity<User> updateUserMoney(
            @PathVariable Long id,
            @RequestParam Long amount) {
        try {
            // update money of user
            User updatedUser = userService.updateMoney(id, amount);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
