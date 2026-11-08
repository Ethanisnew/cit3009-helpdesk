package com.university.helpdesk.controller;

import com.university.helpdesk.entity.model.User;
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
    public ResponseEntity<com.university.helpdesk.entity.model.User> login(@RequestBody LoginRequest request) {

        // Call the service layer to handle the login logic
        User user = userService.login(request.getUsername(), request.getPassword());
        user.setPassword(null);

        // Return a 200 OK status with the user object
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody com.university.helpdesk.dto.RegisterRequest request) {
        User user = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getName(),
                request.getRole()
        );
        user.setPassword(null); // Hide password in response
        return ResponseEntity.ok(user);
    }
}