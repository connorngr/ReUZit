package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.Role;
import com.connorng.ReUzit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Data type of primary key, Long provide more ids for the long term app, it is a common practice
@Repository
public interface UserRepository  extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleNot(Role role);
}
