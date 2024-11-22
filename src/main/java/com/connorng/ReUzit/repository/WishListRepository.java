package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListRepository extends JpaRepository<com.connorng.ReUzit.model.WishList, Long> {
    List<WishList> findAllByUser(User user);
    boolean existsByUserAndListingId(User user, Long listingId);
    WishList findByUserEmailAndListingId(String email, Long idListing);
    Optional<WishList> findByUserIdAndListingId(Long userId, Long listingId);
}
