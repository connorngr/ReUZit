package com.connorng.ReUzit.service;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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


    public String downloadAndSaveImage(String imageUrl) throws IOException {
        // Ensure the upload directory exists
        Files.createDirectories(Paths.get(uploadDir));

        // Download the image
        InputStream in = new URL(imageUrl).openStream();

        // Extract the file name from the URL
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

        // Define the file path where the image will be saved
        Path targetPath = Paths.get(uploadDir, fileName);

        // Save the image to the target directory
        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Close the input stream
        in.close();

        return "/uploads/" + fileName; // Return the path where the image was saved
    }
}
