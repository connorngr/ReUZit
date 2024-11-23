package com.connorng.ReUzit.controller.auth;


import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String image_url;
}
