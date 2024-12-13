package com.connorng.ReUzit.controller.auth;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Setter
@Getter
public class RegisterRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private MultipartFile file;
    private String imageUrl;
}
