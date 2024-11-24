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
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        Optional<Listing> listingOptional = checkIfListingExists(listingId);
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        User user = userOptional.get();
        Long userId = user.getId();

        WishList wishList = new WishList();
        wishList.setUser(user);
        wishList.setListing(listingOptional.get());

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
        Optional<User> userOptional = userService.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        User user = userOptional.get();
        return wishListRepository.findAllByUser(user);
    }

    public boolean isWishListAlready(String email, Long listingId) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

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
        // Find and delete SelectedListing by email and listingId
        WishList wishList = wishListRepository.findByUserEmailAndListingId(email, listingId);

        if (wishList != null) {
            wishListRepository.delete(wishList);
        } else {
            throw new IllegalArgumentException("Selected listing not found.");
        }
    }

    public void deleteWishListByUserAndListing(Long userId, Long listingId) {
        // Find SelectedListing by userId và listingId
        Optional<WishList> wishListOptional = wishListRepository.findByUserIdAndListingId(userId, listingId);

        if (wishListOptional.isPresent()) {
            wishListRepository.delete(wishListOptional.get());
        } else {
            throw new IllegalArgumentException("Selected listing not found for userId: " + userId + " and listingId: " + listingId);
        }
    }

}
