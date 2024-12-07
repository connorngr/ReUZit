package com.connorng.ReUzit.controller.Image;

import com.connorng.ReUzit.model.Image;
import com.connorng.ReUzit.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/add")
    public ResponseEntity<List<Image>> addImage(
            @RequestParam Long listingId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        List<Image> addedImages = imageService.addImages(listingId, files);
        return ResponseEntity.ok(addedImages);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestBody List<Long> ids) {
        imageService.deleteImages(ids);
        return ResponseEntity.ok("Images deleted successfully.");
    }

    @GetMapping("/list/{listingId}")
    public ResponseEntity<List<Image>> getAllImagesByListingId(@PathVariable Long listingId) {
        List<Image> images = imageService.getAllImagesByListingId(listingId);
        return ResponseEntity.ok(images);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Image> updateImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        Image updatedImage = imageService.updateImage(id, file);
        return ResponseEntity.ok(updatedImage);
    }
}
