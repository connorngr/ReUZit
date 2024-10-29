package com.connorng.ReUzit.controller.listing;


import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.service.ListingService;
import com.connorng.ReUzit.service.UserService;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    @Autowired
    private ListingService listingService;
    @Autowired
    private UserService userService;
    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings() {
        List<Listing> listings = listingService.getAllListings();

        listings.forEach(listing -> {
            listing.setCategoryId(listing.getCategory() != null ? listing.getCategory().getId() : null);
        });
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListingById(@PathVariable Long id) {
        String email = userService.getCurrentUserEmail();

        Optional<Listing> listing = listingService.getListingById(id, email);

        if (listing.isPresent()) {
            Listing result = listing.get();
            result.setCategoryId(result.getCategory() != null ? result.getCategory().getId() : null);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();  // Returns 404 Not Found if listing not found
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<Listing>> getCurrentUserListings() {
        // Get the current logged-in user's authentication
        String email = userService.getCurrentUserEmail();

        List<Listing> listings = listingService.getListingsByUserEmail(email);

        listings.forEach(listing -> {
            listing.setCategoryId(listing.getCategory() != null ? listing.getCategory().getId() : null);
        });

        if (listings.isEmpty()) {
            return ResponseEntity.noContent().build();  // Returns 204 No Content if no listings found
        }

        return ResponseEntity.ok(listings);  // Returns 200 OK with listings data
    }

    private static final Logger logger = LoggerFactory.getLogger(ListingController.class);

    @PostMapping
    public ResponseEntity<Listing> createListing(@ModelAttribute ListingRequest listing) throws IOException {
        // Get the current authenticated user
        String email = userService.getCurrentUserEmail();

        // Call the service to handle the listing creation
        Listing createdListing = listingService.createListing(listing, email);
        return ResponseEntity.ok(createdListing);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Listing> updateListing(@PathVariable Long id,
                                                 @ModelAttribute ListingRequest listing) throws IOException {
        // Get the current authenticated user's email
        String email = userService.getCurrentUserEmail();

        // Delegate the update process to the service layer
        Listing updatedListing = listingService.updateListing(id, listing, email);
        updatedListing.setCategoryId(updatedListing.getCategory() != null ? updatedListing.getCategory().getId() : null);
        return ResponseEntity.ok(updatedListing);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteListings(@RequestParam String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        String email = userService.getCurrentUserEmail();
        List<Long> failedDeletions = listingService.deleteListings(idList, email);

        if (failedDeletions.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).build();
        }
    }
    // service AWS
    @PostMapping(
            value = "{id}/listing-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void uploadListingImage(
            @PathVariable("id") Long id,
            @RequestParam("files")List<MultipartFile> files
            ){
        listingService.uploadListingImage(files, id);
    }

    @GetMapping("{id}/listing-image")
    public List<byte[]> getListingImage(
            @PathVariable("id") Long id
    ){
        return listingService.getListingImages(id);
    }
}