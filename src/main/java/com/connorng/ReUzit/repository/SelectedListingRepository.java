package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.SelectedListing;
import com.connorng.ReUzit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelectedListingRepository extends JpaRepository<SelectedListing, Long> {
    List<SelectedListing> findAllByUser(User user);
    boolean existsByUserAndListingId(User user, Long listingId);
    SelectedListing findByUserEmailAndListingId(String email, Long idListing);
}
