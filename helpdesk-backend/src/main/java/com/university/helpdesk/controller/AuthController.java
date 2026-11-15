package com.university.helpdesk.controller;

import com.university.helpdesk.dto.LoginRequest;
import com.university.helpdesk.dto.RegisterRequest;
import com.university.helpdesk.dto.UserResponse;
import com.university.helpdesk.entity.model.Role;
import com.university.helpdesk.entity.model.User;
import com.university.helpdesk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        Role role = request.getRole() != null ? request.getRole() : Role.STUDENT_STAFF;

        User user = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getFullName(),
                request.getEmail(),
                role
        );
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping("/{id}/logout")
    public ResponseEntity<Void> logout(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}