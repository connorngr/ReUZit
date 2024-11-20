package com.connorng.ReUzit.controller.selectedListing;

import com.connorng.ReUzit.model.SelectedListing;
import com.connorng.ReUzit.service.SelectedListingService;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selected-listings")
public class SelectedListingController {

    @Autowired
    private SelectedListingService selectedListingService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<SelectedListing> addSelectedListing(@RequestParam Long listingId) {
        String email = userService.getCurrentUserEmail();

        try {
            SelectedListing selectedListing = selectedListingService.addToSelectedListings(email, listingId);
            return ResponseEntity.ok(selectedListing);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAllSelectedListings() {
        String email = userService.getCurrentUserEmail();

        try {
            List<SelectedListing> selectedListings = selectedListingService.getAllSelectedListingsByUserEmail(email);
            return ResponseEntity.ok(selectedListings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteSelectedListing(@RequestParam Long listingId) {
        String email = userService.getCurrentUserEmail();

        try {
            selectedListingService.deleteSelectedListingByListingId(email, listingId);
            return ResponseEntity.ok("Selected listing deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkIfListingIsSelected(@RequestParam Long listingId) {
        String email = userService.getCurrentUserEmail();

        try {
            boolean exists = selectedListingService.isListingAlreadySelected(email, listingId);
            return ResponseEntity.ok(exists);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

}
