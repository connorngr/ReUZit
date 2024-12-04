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
    @Query("SELECT l FROM Listing l WHERE l.status = :status AND l.user.email != :email")
    List<Listing> findByStatusAndNotUserEmail(@Param("status") Status status, @Param("email") String email);
}
