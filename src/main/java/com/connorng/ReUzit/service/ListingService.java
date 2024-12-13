package com.connorng.ReUzit.service;

import com.connorng.ReUzit.controller.listing.ListingRequest;
import com.connorng.ReUzit.controller.listing.ListingUpdateRequest;
import com.connorng.ReUzit.model.*;
import com.connorng.ReUzit.controller.listing.ListingUpdateRequest;
import com.connorng.ReUzit.dto.ListingDTO;
import com.connorng.ReUzit.model.*;
import com.connorng.ReUzit.repository.ListingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.connorng.ReUzit.common.FileStorageService;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public List<Listing> getAllActiveListingsLogout() {
        return listingRepository.findByStatus(Status.ACTIVE);
    }
    public List<Listing> getAllActiveListings(String authenticatedEmail) {
        return listingRepository.findActiveListingsByStatusAndNotUserEmail(Status.ACTIVE, authenticatedEmail);
    }

    public List<Listing> getAllActiveListingsExcludingUser(Long userId) {
        return listingRepository.findAllActiveListingsExcludingUser(Status.ACTIVE, userId);
    }

    public List<Listing> getListingsByCategoryIdAndActiveStatus(Long categoryId) {
        return listingRepository.findByCategoryIdAndStatus(categoryId, Status.ACTIVE);
    }

    public List<Listing> getActiveListingsByCategoryIdAndNotUser(Long categoryId, Long userId) {
        return listingRepository.findActiveListingsByCategoryIdAndNotUser(categoryId, Status.ACTIVE, userId);
    }

    public List<Listing> getAllListingsByUser(String email) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        return listingRepository.findAllByUserIdAndNotDeleted(userOptional.get().getId());
    }

    public Listing findById(Long id) {
        return listingRepository.findById(id).orElse(null);
    }

    public Optional<ListingDTO> getListingById(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + listingId));
        return Optional.of(convertToDTO(listing));
    }

    public Listing saveListing(Listing listing) {
        return listingRepository.save(listing);
    }

    public ListingDTO createListing(ListingRequest listing, String authenticatedEmail) throws IOException {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        // Fetch the category by its ID
        Optional<Category> categoryOptional = categoryService.findById(listing.getCategoryId());
        if (!categoryOptional.isPresent()) {
            throw new IllegalArgumentException("Category not found.");
        }

        User user = userOptional.get();
        user.setMoney(user.getMoney() - 5000);
        userService.save(user);

        User admin = userService.findFirstByRole(Roles.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        // add money for admin
        admin.setMoney(admin.getMoney() + 5000);
        User userAdmin = userService.save(admin);

        // Create the listing and associate the fetched user and category
        Listing new_listing = new Listing();
        new_listing.setTitle(listing.getTitle());
        new_listing.setDescription(listing.getDescription());
        new_listing.setPrice(listing.getPrice());
        new_listing.setCondition(Condition.valueOf(listing.getCondition()));
        new_listing.setStatus(Status.ACTIVE);
        new_listing.setUser(user);  // Set the authenticated user
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
        Listing savedListing = listingRepository.save(new_listing);
        return convertToDTO(savedListing);
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
            listing.setCondition(Condition.valueOf(listingUpdateRequest.getCondition()));
        }

        return listingRepository.save(listing);
    }

    public List<Listing> deleteListings(List<Long> ids, String authenticatedEmail) {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        User authenticatedUser = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Listing> failedDeletions = new ArrayList<>();
        boolean isAdmin = authenticatedUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        for (Long listingId : ids) {
            Optional<Listing> listingOptional = null;
            try {
                listingOptional = checkIfListingExists(listingId);
                Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing with ID " + listingId + " not found"));

                // Check if the listing is pending
                if (listing.getStatus() == Status.PENDING) {
                    throw new IllegalStateException("Listing with ID " + listingId + " cannot be deleted because it is in 'PENDING' status.");
                }

                if (!isAdmin && !listing.getUser().getId().equals(authenticatedUser.getId())) {
                    throw new SecurityException("You are not authorized to delete listing with ID " + listingId);
                }

                // Perform soft delete by setting isDeleted flag
                listing.setDeleted(true);
                listingRepository.save(listing);

            } catch (Exception e) {
                failedDeletions.add(listingOptional.orElse(null));
                System.out.println("Failed to delete listing with ID " + listingId + ": " + e.getMessage());
            }
        }
        return failedDeletions;
    }

    private Optional<Listing> checkIfListingExists(Long listingId) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (!listingOptional.isPresent()) {
            throw new IllegalArgumentException("Listing not found.");
        }
        return listingOptional;
    }

    private ListingDTO convertToDTO(Listing listing) {
        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId());
        dto.setUserId(listing.getUser().getId());
        dto.setUsername(listing.getUser().getUsername());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setPrice(Long.valueOf(listing.getPrice()));
        dto.setCategoryId(listing.getCategory().getId());
        dto.setCategoryName(listing.getCategory().getName());
        dto.setCondition(String.valueOf(listing.getCondition()));
        dto.setStatus(String.valueOf(listing.getStatus()));
        dto.setCreatedAt(listing.getCreatedAt());
        dto.setUpdatedAt(listing.getUpdatedAt());
        dto.setImages(listing.getImages());
        return dto;
    }

}
