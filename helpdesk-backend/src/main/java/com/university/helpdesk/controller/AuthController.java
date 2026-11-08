package com.university.helpdesk.controller;

import com.university.helpdesk.dto.LoginRequest;
import com.university.helpdesk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // POST endpoint for login
    @PostMapping("/login")
    public ResponseEntity<com.helpdesk.model.User> login(@RequestBody LoginRequest request) {

        // Call the service layer to handle the login logic
        User user = userService.login(request.getUsername(), request.getPassword());
        user.setPassword(null);

        // Return a 200 OK status with the user object
        return ResponseEntity.ok(user);
    }
}