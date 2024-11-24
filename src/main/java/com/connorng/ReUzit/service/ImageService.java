package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.Image;
import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.repository.ImageRepository;
import com.connorng.ReUzit.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.connorng.ReUzit.Common.FileStorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<Image> addImages(Long listingId, List<MultipartFile> files) throws IOException {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing with id " + listingId + " not found"));

        // Create and add multiple images
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : files) {
            // Save file to storage and get URL (implement `fileStorageService.saveFileToStorage`)
            String imageUrl = fileStorageService.saveFileToStorage(file);

            Image image = new Image();
            image.setListing(listing);
            image.setUrl(imageUrl);
            images.add(image);
        }

        // Update the listing's images and save
        listing.getImages().addAll(images);
        listingRepository.save(listing);

        return images; // Return the list of added images
    }

    public void deleteImages(List<Long> ids) {
        for (Long id : ids) {
            if (imageRepository.existsById(id)) {
                imageRepository.deleteById(id);
            } else {
                throw new IllegalArgumentException("Image with id " + id + " not found");
            }
        }
    }

    public List<Image> getAllImagesByListingId(Long listingId) {
        return imageRepository.findByListingId(listingId);
    }
}
