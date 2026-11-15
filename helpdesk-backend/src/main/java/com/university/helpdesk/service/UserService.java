package com.university.helpdesk.service;

import com.university.helpdesk.entity.model.Administrator;
import com.university.helpdesk.entity.model.Role;
import com.university.helpdesk.entity.model.Technician;
import com.university.helpdesk.entity.model.User;
import com.university.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User login(String username, String password) {
        // Find the user, or throw an exception if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Check password
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }

    public User register(String username, String password, String name, String email, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        User newUser;
        if (role == Role.TECHNICIAN) {
            newUser = new com.university.helpdesk.entity.model.Technician(username, password, name, "General");
        } else if (role == Role.ADMIN) {
            newUser = new com.university.helpdesk.entity.model.Administrator(username, password, name);
        } else {
            newUser = new User(username, password, name, role);
        }
        newUser.setEmail(email);
        return userRepository.save(newUser);
    }
}