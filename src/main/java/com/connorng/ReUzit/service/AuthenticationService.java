package com.connorng.ReUzit.service;
import com.connorng.ReUzit.controller.auth.AuthenticationRequest;
import com.connorng.ReUzit.controller.auth.AuthenticationResponse;
import com.connorng.ReUzit.controller.auth.RegisterRequest;
import com.connorng.ReUzit.model.Roles;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private AuthenticationManager authenticationManager;

    private boolean checkExist(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            // Handle the error (e.g., throw an exception or return an error response)
            throw new IllegalArgumentException("Email is already registered.");
        }
        return true;
    };


    public AuthenticationResponse register(RegisterRequest request, MultipartFile imageUrl) throws IOException {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            // Handle the error (e.g., throw an exception or return an error response)
            throw new IllegalArgumentException("Email is already registered.");
        }
        String imageUrlPath = null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageUrlPath = fileStorageService.saveFileToStorage(imageUrl);
        }
        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.ROLE_USER)
                .imageUrl(imageUrlPath)
                .money(0.0)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    };

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
//        System.out.println(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    };

    public AuthenticationResponse googleAuth(User googleUser) throws IOException {
        Optional<User> existingUser = userRepository.findByEmail(googleUser.getEmail());

        User user;
        if (existingUser.isPresent()) {
            // Use the existing user if they are already registered
            user = existingUser.get();
        } else {
            String savedImagePath = fileStorageService.downloadAndSaveImage(googleUser.getImageUrl());
            // Register the user if they are not in the database
            user = User.builder()
                    .firstName(googleUser.getFirstName())
                    .lastName(googleUser.getLastName())
                    .email(googleUser.getEmail())
                    .imageUrl(savedImagePath) // Assuming the Google user has an image URL
                    .password(passwordEncoder.encode(googleUser.getPassword()))
                    .role(Roles.ROLE_USER)
                    .money(0.0)
                    .build();
            userRepository.save(user);
        }

        // Generate a JWT token for the user
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
