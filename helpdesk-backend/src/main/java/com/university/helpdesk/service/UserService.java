package com.university.helpdesk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private com.helpdesk.repository.UserRepository userRepository;

    public User login(String username, String password) {
        // Find the user, or throw an exception if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Check password (In a real app, use BCrypt. For now, plain text is fine)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }
}