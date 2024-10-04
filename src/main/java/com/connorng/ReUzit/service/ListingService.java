package com.connorng.ReUzit.service;

import com.connorng.ReUzit.controller.listing.ListingRequest;
import com.connorng.ReUzit.model.Category;
import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Optional;
//Add admin the permission later
@Service
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

    public Listing createListing(ListingRequest listing, String authenticatedEmail) {
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
        return listingRepository.save(new_listing);
    }

    public Listing updateListing(Long listingId, ListingRequest listingRequest, String authenticatedEmail) {
        // Fetch the existing listing from the database
        Optional<Listing> listingOptional = listingRepository.findById(listingId);

        if (!listingOptional.isPresent()) {
            throw new IllegalArgumentException("Listing not found.");
        }

        Listing listing = listingOptional.get();

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

        // Save the updated listing and return
        return listingRepository.save(listing);
    }

    public boolean deleteListing(Long listingId, String authenticatedEmail) {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        User authenticatedUser = userOptional.get();
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (!listingOptional.isPresent()) {
            throw new IllegalArgumentException("Listing not found.");
        }
        //Get the object from optional
        Listing listing = listingOptional.get();
        // Check if the authenticated user owns the listing
        if (!listing.getUser().getId().equals(authenticatedUser.getId())) {
            throw new SecurityException("You are not authorized to update this listing.");
        }
        try {
            listingRepository.deleteById(listingId);
            return true;
        }
        catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
