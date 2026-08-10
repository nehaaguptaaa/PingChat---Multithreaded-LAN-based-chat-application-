package com.chatapp.pingchat.controller;

import com.chatapp.pingchat.dto.AuthResponse;
import com.chatapp.pingchat.dto.LoginRequest;
import com.chatapp.pingchat.dto.RegisterRequest;
import com.chatapp.pingchat.entity.User;
import com.chatapp.pingchat.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")   // LAN ke doosre devices se React app access karega, isliye open rakha
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hash karke store
        user.setOnline(false);

        User saved = userRepository.save(user);

        return ResponseEntity.ok(new AuthResponse(saved.getId(), saved.getUsername(), "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getUsername(), "Login successful"));
    }
}