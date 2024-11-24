package com.connorng.ReUzit.service;

import com.connorng.ReUzit.controller.listing.ListingRequest;
import com.connorng.ReUzit.controller.listing.ListingUpdateRequest;
import com.connorng.ReUzit.model.Category;
import com.connorng.ReUzit.model.Image;
import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.connorng.ReUzit.Common.FileStorageService;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Add admin the permission later
@Service
public class ListingService {
    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileStorageService fileStorageService;

    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    public Listing findById(Long id) {
        return listingRepository.findById(id).orElse(null);
    }

    public Optional<Listing> getListingById(Long listingId) {
        return listingRepository.findById(listingId);
    }

    public List<Listing> getListingsByUserEmail(String userEmail) {
        Optional<User> userOptional = userService.findByEmail(userEmail);

        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        return listingRepository.findByUser(userOptional);
    }

    public Listing createListing(ListingRequest listing, String authenticatedEmail) throws IOException {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        // Fetch the category by its ID
        Optional<Category> categoryOptional = categoryService.findById(listing.getCategoryId());
        if (!categoryOptional.isPresent()) {
            throw new IllegalArgumentException("Category not found.");
        }

        // Create the listing and associate the fetched user and category
        Listing new_listing = new Listing();
        new_listing.setTitle(listing.getTitle());
        new_listing.setDescription(listing.getDescription());
        new_listing.setPrice(listing.getPrice());
        new_listing.setCondition(listing.getCondition());
        new_listing.setStatus(listing.getStatus());
        new_listing.setUser(userOptional.get());  // Set the authenticated user
        new_listing.setCategory(categoryOptional.get());  // Set the associated category

        // Save the listing and return the response
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : listing.getImages()) {
            String imageUrl = fileStorageService.saveFileToStorage(file);  // Implement your logic for saving the file
            Image image = new Image();
            image.setUrl(imageUrl);
            image.setListing(new_listing);
            images.add(image);
        }
        new_listing.setImages(images);
        return listingRepository.save(new_listing);
    }

    public Listing updateListing(Long listingId, ListingUpdateRequest listingUpdateRequest, String authenticatedEmail) throws IOException {
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
        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        System.out.println("Admin have role is: " + isAdmin);
        if (!isAdmin && !listing.getUser().getId().equals(authenticatedUser.getId())) {
            throw new SecurityException("You are not authorized to update listing with ID " + listingId);
        }

        // Optionally update the category
        if (listingUpdateRequest.getCategoryId() != null) {
            Optional<Category> categoryOptional = categoryService.findById(listingUpdateRequest.getCategoryId());
            if (!categoryOptional.isPresent()) {
                throw new IllegalArgumentException("Category not found.");
            }
            listing.setCategory(categoryOptional.get());
        }

        // Update other fields based on the request
        if (listingUpdateRequest.getTitle() != null) {
            listing.setTitle(listingUpdateRequest.getTitle());
        }

        if (listingUpdateRequest.getDescription() != null) {
            listing.setDescription(listingUpdateRequest.getDescription());
        }

        if (listingUpdateRequest.getPrice() != null) {
            listing.setPrice(listingUpdateRequest.getPrice());
        }

        if (listingUpdateRequest.getCondition() != null) {
            listing.setCondition(listingUpdateRequest.getCondition());
        }

        if (listingUpdateRequest.getStatus() != null) {
            listing.setStatus(listingUpdateRequest.getStatus());
        }

        return listingRepository.save(listing);
    }

    public List<Long> deleteListings(List<Long> ids, String authenticatedEmail) {
        // Get information user login
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        User authenticatedUser = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));
        // List don't delete
        List<Long> failedDeletions = new ArrayList<>();
        // Is Admin
        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        for (Long listingId : ids) {
            try {
                // Check listing
                Optional<Listing> listingOptional = checkIfListingExists(listingId);
                Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing with ID " + listingId + " not found"));

                // own listing ?
                if (!isAdmin && !listing.getUser().getId().equals(authenticatedUser.getId())) {
                    throw new SecurityException("You are not authorized to delete listing with ID " + listingId);
                }

                // Delete listing database
                listingRepository.deleteById(listingId);

            } catch (Exception e) {
                // if error print information idListing
                failedDeletions.add(listingId);
                System.out.println("Failed to delete listing with ID " + listingId + ": " + e.getMessage());
            }
        }
        return failedDeletions; // Return list id don't delete, and null if finish
    }
    private Optional<Listing> checkIfListingExists(Long listingId) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (!listingOptional.isPresent()) {
            throw new IllegalArgumentException("Listing not found.");
        }
        return listingOptional;
    }


}
