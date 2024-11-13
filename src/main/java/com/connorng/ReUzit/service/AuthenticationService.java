package com.connorng.ReUzit.service;

import com.connorng.ReUzit.controller.auth.AuthenticationRequest;
import com.connorng.ReUzit.controller.auth.AuthenticationResponse;
import com.connorng.ReUzit.controller.auth.RegisterRequest;
import com.connorng.ReUzit.model.Role;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.connorng.ReUzit.Common.FileStorageService;
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
        System.out.println("Toi da toi day");
        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .imageUrl(imageUrlPath)
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
        System.out.println(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    };
}
