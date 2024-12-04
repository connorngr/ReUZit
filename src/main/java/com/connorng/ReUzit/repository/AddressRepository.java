package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDelete = false")
    List<Address> findByUserIdAndNotDeleted(Long userId);
}
