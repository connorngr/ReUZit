package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.SelectedListing;
import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.SelectedListingRepository;
import com.connorng.ReUzit.repository.ListingRepository;
import com.connorng.ReUzit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SelectedListingService {

    @Autowired
    private SelectedListingRepository selectedListingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ListingRepository listingRepository;

    public SelectedListing addToSelectedListings(String authenticatedEmail, Long listingId) {
        Optional<User> userOptional = userService.findByEmail(authenticatedEmail);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }
        Optional<Listing> listingOptional = checkIfListingExists(listingId);
        Listing listing = listingOptional.orElseThrow(() -> new RuntimeException("Listing not found"));

        User user = userOptional.get();
        Long userId = user.getId();

        SelectedListing selectedListing = new SelectedListing();
        selectedListing.setUser(user);
        selectedListing.setListing(listingOptional.get());

        return selectedListingRepository.save(selectedListing);
    }

    public void deleteSelectedListing(String email, Long selectedListingId) {
        Optional<User> userOptional = userService.findByEmail(email);
        User authenticatedUser = userOptional.orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<SelectedListing> selectedListingOptional = selectedListingRepository.findById(selectedListingId);
        if (!selectedListingOptional.isPresent()) {
            throw new IllegalArgumentException("Selected listing not found.");
        }
        selectedListingRepository.deleteById(selectedListingId);
    }

    public List<SelectedListing> getAllSelectedListingsByUserEmail(String email) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        User user = userOptional.get();
        return selectedListingRepository.findAllByUser(user);
    }

    public boolean isListingAlreadySelected(String email, Long listingId) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        return selectedListingRepository.existsByUserAndListingId(user, listingId);
    }

    private Optional<Listing> checkIfListingExists(Long listingId) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (!listingOptional.isPresent()) {
            throw new IllegalArgumentException("Listing not found.");
        }
        return listingOptional;
    }

    public void deleteSelectedListingByListingId(String email, Long listingId) {
        // Tìm và xóa SelectedListing dựa trên email và listingId
        SelectedListing selectedListing = selectedListingRepository.findByUserEmailAndListingId(email, listingId);

        if (selectedListing != null) {
            selectedListingRepository.delete(selectedListing);
        } else {
            throw new IllegalArgumentException("Selected listing not found.");
        }
    }

}
