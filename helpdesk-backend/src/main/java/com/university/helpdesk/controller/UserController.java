package com.university.helpdesk.controller;

import com.university.helpdesk.dto.UserResponse;
import com.university.helpdesk.entity.model.Role;
import com.university.helpdesk.entity.model.User;
import com.university.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody Map<String, String> body) {
        User user = new User(body.get("username"), body.get("password"), body.get("fullName"), Role.valueOf(body.get("role")));
        user.setEmail(body.get("email"));
        return ResponseEntity.ok(new UserResponse(userRepository.save(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(body.get("fullName"));
        user.setRole(Role.valueOf(body.get("role")));
        user.setEmail(body.get("email"));
        return ResponseEntity.ok(new UserResponse(userRepository.save(user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}