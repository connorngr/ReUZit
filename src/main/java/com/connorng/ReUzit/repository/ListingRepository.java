package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByUser(Optional<User> user);
}
