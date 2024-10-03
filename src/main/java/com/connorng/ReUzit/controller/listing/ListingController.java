package com.connorng.ReUzit.controller.listing;


import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    @Autowired
    private ListingService listingService;

    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings() {
        return ResponseEntity.ok(listingService.getAllListings());
    }

    @PostMapping
    public ResponseEntity<Listing> createListing(@RequestBody ListingRequest listing) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();  // Assuming the email is used as username
        }

        // Call the service to handle the listing creation
        Listing createdListing = listingService.createListing(listing, email);

        return ResponseEntity.ok(createdListing);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Listing> updateListing(@PathVariable Long id,
                                                 @RequestBody ListingRequest listingRequest) {
        // Get the current authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();  // Assuming email is used as username
        }

        // Delegate the update process to the service layer
        Listing updatedListing = listingService.updateListing(id, listingRequest, email);

        return ResponseEntity.ok(updatedListing);
    }
}