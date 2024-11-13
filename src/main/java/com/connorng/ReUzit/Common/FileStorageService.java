package com.connorng.ReUzit.Common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "src/main/resources/static/uploads";

    public String saveFileToStorage(MultipartFile file) throws IOException {

        // Generate a unique filename to avoid filename collisions
        String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        // Create the upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Create the complete file path
        Path filePath = uploadPath.resolve(filename);

        // Save the file to the specified path
        Files.copy(file.getInputStream(), filePath);

        // Return the relative path where the file is saved
        return "/uploads/" + filename; // Adjust the URL as necessary for your application
    }
}

