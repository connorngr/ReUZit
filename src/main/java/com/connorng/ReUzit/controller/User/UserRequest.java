package com.connorng.ReUzit.controller.User;

import org.springframework.web.multipart.MultipartFile;

public class UserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private MultipartFile imageUrl;
}
