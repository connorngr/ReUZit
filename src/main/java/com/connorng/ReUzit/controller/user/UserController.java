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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        return userService.save(user);
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
            Optional<User> userOptional = userService.findById(id);
            // update money of user
            User user = userOptional.get();
            user.setMoney(user.getMoney() + amount);

            return ResponseEntity.ok(userService.save(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserUpdateRequest request) {
        String email = userService.getCurrentUserEmail();
        Optional<User> optionalUser = userService.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        // Update user information
        User updatedUser = userService.updateUserInfo(
                user.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getBio()
        );

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/image")
    public ResponseEntity<User> updateUserImage(
            @RequestParam("image") MultipartFile file) throws IOException {
        String email = userService.getCurrentUserEmail();

        User updatedUser = userService.updateUserImage(email, file);
        return ResponseEntity.ok(updatedUser);
    }

}
