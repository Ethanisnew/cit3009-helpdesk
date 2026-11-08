package com.university.helpdesk.controller;

import com.university.helpdesk.dto.UserResponse;
import com.university.helpdesk.entity.model.User;
import com.university.helpdesk.dto.LoginRequest;
import com.university.helpdesk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    @Autowired
    private UserService userService;

    // POST endpoint for login
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new UserResponse(user));
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