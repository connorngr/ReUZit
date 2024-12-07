package com.connorng.ReUzit.service;

import com.connorng.ReUzit.common.FileStorageService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
                .money(0L)
                .addresses(new ArrayList<>())
                .bio(null)
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
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    };

    public AuthenticationResponse googleAuth(User googleUser) {
        if (!checkExist(googleUser.getEmail())) {
            User user = User.builder()
                    .firstName(googleUser.getFirstName())
                    .lastName(googleUser.getLastName())
                    .email(googleUser.getEmail())
                    .password(passwordEncoder.encode(googleUser.getPassword())) // Use a generated password or secure default
                    .role(Roles.ROLE_USER)
                    .imageUrl(googleUser.getImageUrl())
                    .money(0L)
                    .addresses(new ArrayList<>())
                    .build();
            userRepository.save(user);

            String jwtToken = jwtService.generateToken(user);
            System.out.println("Token received: " + jwtToken);
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        }

        // Authenticate existing user
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                googleUser.getEmail(),
                googleUser.getPassword()
        );
        return authenticate(authenticationRequest);
    }
}
