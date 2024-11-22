package com.connorng.ReUzit.controller.wishList;

import com.connorng.ReUzit.service.WishListService;
import com.connorng.ReUzit.model.WishList;
import com.connorng.ReUzit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishList")
public class WishListController {

    @Autowired
    private WishListService wishListService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<WishList> addSelectedListing(@RequestParam Long listingId) {
        String email = userService.getCurrentUserEmail();

        try {
            WishList selectedListing = wishListService.addToWishList(email, listingId);
            return ResponseEntity.ok(selectedListing);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAllSelectedListings() {
        String email = userService.getCurrentUserEmail();

        try {
            List<WishList> selectedListings = wishListService.getAllWishListByUserEmail(email);
            return ResponseEntity.ok(selectedListings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteSelectedListing(@RequestParam Long listingId) {
        String email = userService.getCurrentUserEmail();

        try {
            wishListService.deleteWishListByListingId(email, listingId);
            return ResponseEntity.ok("Selected listing deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkIfListingIsSelected(@RequestParam Long listingId) {
        String email = userService.getCurrentUserEmail();

        try {
            boolean exists = wishListService.isWishListAlready(email, listingId);
            return ResponseEntity.ok(exists);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

}
