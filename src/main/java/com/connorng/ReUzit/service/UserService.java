package com.connorng.ReUzit.service;

import com.connorng.ReUzit.common.FileStorageService;
import com.connorng.ReUzit.model.Roles;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User createUser(User user) {
        return userRepository.save(user);
    }
    public Optional<User> findByEmail (String email) {return userRepository.findByEmail(email);}
    public Optional<User> findByNameOrEmail(String name, String email) {
        // Tìm kiếm user dựa trên kết hợp firstName + lastName hoặc email
        return userRepository.findByFirstNameLastNameOrEmail(name, email);
    }

    public Optional<User> findByRole(Roles role) {
        return userRepository.findByRole(role);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public String getCurrentUserEmail() {
        // Get the current authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getEmail();
        }
        return null;
    }

    public List<User> getAllNonAdminUsers() {
        return userRepository.findByRoleNot(Roles.ROLE_ADMIN);
    }

    @Transactional
    public User toggleUserLock(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setLocked(!user.isLocked());
        return userRepository.save(user);
    }

    public User updateMoney(Long userId, Long amount) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // upadate money
        user.setMoney(user.getMoney() + amount);

        // Save change
        return userRepository.save(user);
    }

    public User updateMoney(String email, Long amount) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        // upadate money
        user.setMoney(user.getMoney() + amount);
        // Save change
        return userRepository.save(user);
    }

    public User updateUserInfo(Long userId, String firstName, String lastName, String bio) {
        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Update fields
        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            user.setLastName(lastName);
        }
        if (bio != null) {
            user.setBio(bio);
        }

        // Save updated user
        return userRepository.save(user);
    }

    public User updateUserImage(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        String imageUrl = fileStorageService.saveFileToStorage(file);

        user.setImageUrl(imageUrl);

        return userRepository.save(user);
    }

}
