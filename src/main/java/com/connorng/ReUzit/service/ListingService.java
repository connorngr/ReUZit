package com.connorng.ReUzit.service;

import com.connorng.ReUzit.controller.listing.ListingRequest;
import com.connorng.ReUzit.model.Category;
import com.connorng.ReUzit.model.Image;
import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.ListingRepository;
import com.connorng.ReUzit.s3.S3Buckets;
import com.connorng.ReUzit.s3.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Add admin the permission later
@Service
//@Transactional
public class ListingService {
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    @Autowired
    private S3Service s3Service;

    @Autowired
    private S3Buckets s3Buckets;

    public Listing createListing(ListingRequest listingRequest, String authenticatedEmail, List<MultipartFile> listingImageFiles) {
        // Step 1: Find the user by email
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        // Step 2: Fetch the category by its ID
        Optional<Category> categoryOptional = categoryService.findById(listingRequest.getCategoryId());
        if (!categoryOptional.isPresent()) {
            throw new IllegalArgumentException("Category not found.");
        }

        // Step 3: Create the listing and set the associated user and category
        Listing newListing = new Listing();
        newListing.setTitle(listingRequest.getTitle());
        newListing.setDescription(listingRequest.getDescription());
        newListing.setPrice(listingRequest.getPrice());
        newListing.setCondition(listingRequest.getCondition());
        newListing.setStatus(listingRequest.getStatus());
        newListing.setUser(userOptional.get());  // Set the authenticated user
        newListing.setCategory(categoryOptional.get());  // Set the associated category

        // Step 4: Save the listing first to get the listing ID
        Listing savedListing = listingRepository.save(newListing);

        // Step 5: If there are images, proceed to upload them
        if (listingImageFiles != null && !listingImageFiles.isEmpty()) {
            for (MultipartFile file : listingImageFiles) {
                String listingImageId = UUID.randomUUID().toString();  // Generate a new image ID
                String fileName = "listing-images/%s/%s".formatted(savedListing.getId(), listingImageId);  // File path in S3

                try {
                    // Upload the image to S3
                    s3Service.putObject(
                            s3Buckets.getListing(),
                            fileName,
                            file.getBytes()
                    );

                    // Create an Image entity and associate it with the saved listing
                    Image image = new Image();
                    image.setListing(savedListing);
                    image.setUrl(listingImageId);  // Use listingImageId as the key in the URL

                    // Add the image to the listing
                    savedListing.getImages().add(image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image", e);
                }
            }

            // Save the listing again with the images
            listingRepository.save(savedListing);
        }

        // Step 6: Return the saved listing with its images
        return savedListing;
    }


    public Listing updateListing(Long listingId, ListingRequest listingRequest, String authenticatedEmail, List<MultipartFile> listingImageFiles) {
        // Fetch the existing listing from the database
        Optional<Listing> listingOptional = checkIfListingExists(listingId);
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        // Fetch the authenticated user
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        User authenticatedUser = userOptional.get();

        // Check if the authenticated user owns the listing
        if (!listing.getUser().getId().equals(authenticatedUser.getId())) {
            throw new SecurityException("You are not authorized to update this listing.");
        }

        // Optionally update the category
        if (listingRequest.getCategoryId() != null) {
            Optional<Category> categoryOptional = categoryService.findById(listingRequest.getCategoryId());
            if (!categoryOptional.isPresent()) {
                throw new IllegalArgumentException("Category not found.");
            }
            listing.setCategory(categoryOptional.get());
        }

        // Update other fields based on the request
        if (listingRequest.getTitle() != null) {
            listing.setTitle(listingRequest.getTitle());
        }
        if (listingRequest.getDescription() != null) {
            listing.setDescription(listingRequest.getDescription());
        }
        if (listingRequest.getPrice() != null) {
            listing.setPrice(listingRequest.getPrice());
        }
        if (listingRequest.getCondition() != null) {
            listing.setCondition(listingRequest.getCondition());
        }
        if (listingRequest.getStatus() != null) {
            listing.setStatus(listingRequest.getStatus());
        }

        // Handle image updates
        if (listingImageFiles != null && !listingImageFiles.isEmpty()) {
            // Step 1: Remove old images from S3 and the database
            if (listing.getImages() != null && !listing.getImages().isEmpty()) {
//                for (Image oldImage : listing.getImages()) {
//                    String filePath = "listing-images/%s/%s".formatted(listing.getId(), oldImage.getUrl());
//                    s3Service.deleteObject(s3Buckets.getListing(), filePath); // Remove from S3
//                }
                listing.getImages().clear();  // Clear the images in the database
            }

            // Step 2: Upload new images
            for (MultipartFile file : listingImageFiles) {
                String listingImageId = UUID.randomUUID().toString();  // Generate a new image ID
                String fileName = "listing-images/%s/%s".formatted(listingId, listingImageId);  // File path in S3

                try {
                    // Upload the image to S3
                    s3Service.putObject(
                            s3Buckets.getListing(),
                            fileName,
                            file.getBytes()
                    );

                    // Create a new Image entity and associate it with the listing
                    Image image = new Image();
                    image.setListing(listing);
                    image.setUrl(listingImageId);  // Use listingImageId as the key in the URL

                    // Add the new image to the listing
                    listing.getImages().add(image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image", e);
                }
            }
        }

        // Save the updated listing and return
        return listingRepository.save(listing);
    }


    public boolean deleteListing(Long listingId, String authenticatedEmail) {
        // Fetch the authenticated user
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        User authenticatedUser = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the listing exists
        Optional<Listing> listingOptional = checkIfListingExists(listingId);
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        // Check if the authenticated user owns the listing
        if (!listing.getUser().getId().equals(authenticatedUser.getId())) {
            throw new SecurityException("You are not authorized to delete this listing.");
        }

        try {
            // Step 1: Remove associated images from S3
//            if (listing.getImages() != null && !listing.getImages().isEmpty()) {
//                for (Image image : listing.getImages()) {
//                    String filePath = "listing-images/%s/%s".formatted(listing.getId(), image.getUrl());
//                    s3Service.deleteObject(s3Buckets.getListing(), filePath);  // Remove image from S3
//                }
//            }

            // Step 2: Delete the listing from the database
            listingRepository.deleteById(listingId);
            return true;

        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete listing and associated images", e);
        }
    }


    private Optional<Listing> checkIfListingExists(Long listingId) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (!listingOptional.isPresent()) {
            throw new IllegalArgumentException("Listing not found.");
        }
        return listingOptional;
    }


    public void uploadListingImage(List<MultipartFile> listingImageFiles, Long id) {
        Optional<Listing> listingOptional = checkIfListingExists(id); // Check if the listing exists
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        // Step 1: Remove old images from S3 and the database
        if (listing.getImages() != null && !listing.getImages().isEmpty()) {
            // Clear old images from the listing
            listing.getImages().clear();
        }

        // Step 2: Add new images
        for (MultipartFile file : listingImageFiles) {
            String listingImageId = UUID.randomUUID().toString(); // Generate a new image ID
            String fileName = "listing-images/%s/%s".formatted(id, listingImageId); // File path in S3

            try {
                // Upload the image to S3
                s3Service.putObject(
                        s3Buckets.getListing(),
                        fileName,
                        file.getBytes()
                );

                // Step 3: Create a new Image entity and set the URL as listingImageId (key)
                Image image = new Image();
                image.setListing(listing);
                image.setUrl(listingImageId); // Set the URL as the key (listingImageId)

                // Add new image to the listing
                listing.getImages().add(image);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        // Step 4: Save the updated listing
        listingRepository.save(listing);
    }

    public List<byte[]> getListingImages(Long id) {
        // Step 1: Check if the listing exists
        Optional<Listing> listingOptional = checkIfListingExists(id);
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        // Step 2: Check if there are any images for the listing
        if (listing.getImages() == null || listing.getImages().isEmpty()) {
            throw new RuntimeException("No images found for this listing");
        }

        // Step 3: Initialize a list to store image data
        List<byte[]> listingImages = new ArrayList<>();

        // Step 4: Retrieve each image from S3 using the listingImageId (stored in Image.url)
        for (Image image : listing.getImages()) {
            String listingImageId = image.getUrl();  // This is where we store the S3 key (listingImageId)
            byte[] listingImage = s3Service.getObject(
                    s3Buckets.getListing(),
                    "listing-images/%s/%s".formatted(id, listingImageId) // Fetch image using listing id and image id
            );
            listingImages.add(listingImage);
        }

        // Step 5: Return the list of images (as byte arrays)
        return listingImages;
    }

}
