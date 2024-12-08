package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.model.WishList;
import com.connorng.ReUzit.repository.ListingRepository;
import com.connorng.ReUzit.repository.WishListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WishListService {

    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ListingRepository listingRepository;

    public WishList addToWishList(String authenticatedEmail, Long listingId) {
        // Fetch the authenticated user.
        User user = userService.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Check if the listing exists.
        Listing listing = checkIfListingExists(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Check if a wishlist entry already exists for the user and listing.
        Optional<WishList> existingWishList = wishListRepository.findByUserAndListingId(user.getId(), listingId);

        if (existingWishList.isPresent()) {
            WishList wishList = existingWishList.get();
            if (wishList.isDelete()) {
                // Update isDelete to false if the entry was marked as deleted.
                wishList.setDelete(false);
                return wishListRepository.save(wishList);
            } else {
                // Entry already exists and is active; return it without changes.
                return wishList;
            }
        }

        // Create a new wishlist entry if it doesn't exist.
        WishList wishList = new WishList();
        wishList.setUser(user);
        wishList.setListing(listing);
        return wishListRepository.save(wishList);
    }


    public void deleteWishList(String email, Long wishListId) {
        Optional<User> userOptional = userService.findByEmail(email);
        User authenticatedUser = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<WishList> wishListOptional = wishListRepository.findById(wishListId);
        if (!wishListOptional.isPresent()) {
            throw new IllegalArgumentException("Selected listing not found.");
        }
        wishListRepository.deleteById(wishListId);
    }

    public List<WishList> getAllWishListByUserEmail(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Retrieve only active wishlist entries (isDelete = false).
        return wishListRepository.findAllByUser(user);
    }


    public boolean isWishListAlready(String email, Long listingId) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // Returns true if the listing is actively selected (isDelete = false).
        return wishListRepository.existsByUserAndListingId(user, listingId);
    }

    private Optional<Listing> checkIfListingExists(Long listingId) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (!listingOptional.isPresent()) {
            throw new IllegalArgumentException("Listing not found.");
        }
        return listingOptional;
    }

        public void deleteWishListByListingId(String email, Long listingId) {
            User currentUser = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            WishList wishList = wishListRepository.findByUserAndListingIdAndDeleteFalse(currentUser.getId(), listingId)
                    .orElseThrow(() -> new IllegalArgumentException("Wishlist entry not found"));

            wishList.setDelete(true); // Mark the entry as deleted.
            wishListRepository.save(wishList); // Save the updated entity.
        }

}
