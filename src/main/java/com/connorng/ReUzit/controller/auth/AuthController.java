package com.connorng.ReUzit.controller.auth;

import com.connorng.ReUzit.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Console;
import java.io.DataInput;
import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AuthenticationResponse> register(
            @RequestParam("user") String userJson,
            @RequestParam("imageUrl") MultipartFile file) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        RegisterRequest registerRequest = objectMapper.readValue(userJson, RegisterRequest.class);

        AuthenticationResponse response = authenticationService.register(registerRequest, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    };
}
