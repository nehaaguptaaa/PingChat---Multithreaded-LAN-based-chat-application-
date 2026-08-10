package com.chatapp.pingchat.controller;

import com.chatapp.pingchat.dto.AuthResponse;
import com.chatapp.pingchat.dto.LoginRequest;
import com.chatapp.pingchat.dto.RegisterRequest;
import com.chatapp.pingchat.entity.User;
import com.chatapp.pingchat.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed — username already taken: {}", request.getUsername());
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOnline(false);

        User saved = userRepository.save(user);
        logger.info("New user registered: {}", saved.getUsername());

        return ResponseEntity.ok(new AuthResponse(saved.getId(), saved.getUsername(), "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Failed login attempt for username: {}", request.getUsername());
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        logger.info("User logged in: {}", user.getUsername());
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getUsername(), "Login successful"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Validation failed");
        logger.warn("Validation failed: {}", errorMessage);
        return ResponseEntity.badRequest().body(errorMessage);
    }
}