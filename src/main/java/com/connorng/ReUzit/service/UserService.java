package com.connorng.ReUzit.service;

import com.connorng.ReUzit.model.User;
import com.connorng.ReUzit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User createUser(User user) {
        return userRepository.save(user);
    }
    public Optional<User> findByEmail (String email) {return userRepository.findByEmail(email);}
}
