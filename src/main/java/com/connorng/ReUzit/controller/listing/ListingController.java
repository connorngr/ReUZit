package com.connorng.ReUzit.controller.listing;


import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.service.ListingService;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    @Autowired
    private ListingService listingService;
    @Autowired
    private UserService userService;
//    @Autowired
//    private Listing listing;
    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings() {
        return ResponseEntity.ok(listingService.getAllListings());
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Listing> createListing(
            @RequestPart("listingRequest") ListingRequest listingRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> listingImageFiles) {

        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();  // Assuming the email is used as username
        }

        // If no images were provided, set the list to an empty one
        if (listingImageFiles == null) {
            listingImageFiles = Collections.emptyList();
        }

        // Call the service to handle the listing creation with images
        Listing createdListing = listingService.createListing(listingRequest, email, listingImageFiles);

        return ResponseEntity.ok(createdListing);
    }


    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Listing> updateListing(@PathVariable Long id,
                                                 @RequestPart("listingRequest") ListingRequest listingRequest,
                                                 @RequestPart("images") List<MultipartFile> listingImageFiles) {
        // Get the current authenticated user's email
        String email = userService.getCurrentUserEmail();

        // Delegate the update process to the service layer
        Listing updatedListing = listingService.updateListing(id, listingRequest, email, listingImageFiles);

        return ResponseEntity.ok(updatedListing);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long id) {
        // Get the current authenticated user's email
        String email = userService.getCurrentUserEmail();
        boolean isDeleted = listingService.deleteListing(id, email);

        if (isDeleted) {
            return ResponseEntity.noContent().build();  // 204 No Content if the deletion was successful
        } else {
            return ResponseEntity.notFound().build();   // 404 Not Found if the listing with the given id doesn't exist
        }
    }

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