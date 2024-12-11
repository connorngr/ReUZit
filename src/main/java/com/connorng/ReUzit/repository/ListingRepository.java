package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Listing;
import com.connorng.ReUzit.model.Status;
import com.connorng.ReUzit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByUser(Optional<User> user);

    List<Listing> findByStatus(Status status);

    List<Listing> findByUserId(Long userId);

    @Query("SELECT l FROM Listing l WHERE l.isDeleted = false AND l.category.id = :categoryId AND l.status = :status")
    List<Listing> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") Status status);

    @Query("SELECT l FROM Listing l WHERE l.isDeleted = false AND l.status = :status AND l.user.id != :userId")
    List<Listing> findActiveListingsByStatusAndNotUserId(@Param("status") Status status, @Param("userId") Long userId);

    @Query("SELECT l FROM Listing l WHERE l.isDeleted = false AND l.status = :status AND l.user.id != :userId")
    List<Listing> findAllActiveListingsExcludingUser(@Param("status") Status status, @Param("userId") Long userId);

    @Query("SELECT l FROM Listing l WHERE l.isDeleted = false AND l.status = :status AND l.user.email != :email")
    List<Listing> findActiveListingsByStatusAndNotUserEmail(@Param("status") Status status, @Param("email") String email);

    @Query("SELECT l FROM Listing l WHERE l.id = :id AND l.isDeleted = false")
    Optional<Listing> findActiveListingById(@Param("id") Long id);

    @Query("SELECT l FROM Listing l WHERE l.isDeleted = false AND l.user.id = :userId")
    List<Listing> findAllByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT l FROM Listing l WHERE l.category.id = :categoryId AND l.status = :status AND l.user.id != :userId AND l.isDeleted = false")
    List<Listing> findActiveListingsByCategoryIdAndNotUser(@Param("categoryId") Long categoryId,
                                                           @Param("status") Status status,
                                                           @Param("userId") Long userId);
}
