package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListRepository extends JpaRepository<com.connorng.ReUzit.model.WishList, Long> {
    @Query("SELECT w FROM WishList w WHERE w.user = :user AND w.isDelete = false")
    List<WishList> findAllByUser(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN TRUE ELSE FALSE END FROM WishList w WHERE w.user = :user AND w.listing.id = :listingId AND w.isDelete = false")
    boolean existsByUserAndListingId(@Param("user") User user, @Param("listingId") Long listingId);

    WishList findByUserEmailAndListingId(String email, Long idListing);

    Optional<WishList> findByUserIdAndListingId(Long userId, Long listingId);

    @Query("SELECT w FROM WishList w WHERE w.user.id = :userId AND w.listing.id = :listingId AND w.isDelete = false")
    Optional<WishList> findByUserAndListingIdAndDeleteFalse(@Param("userId") Long userId, @Param("listingId") Long listingId);

    @Query("SELECT w FROM WishList w WHERE w.user.id = :userId AND w.listing.id = :listingId")
    Optional<WishList> findByUserAndListingId(@Param("userId") Long userId, @Param("listingId") Long listingId);
}
