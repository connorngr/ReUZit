package com.connorng.ReUzit.repository;

import com.connorng.ReUzit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Long> {
}
