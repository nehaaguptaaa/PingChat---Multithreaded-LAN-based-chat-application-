package com.chatapp.pingchat.controller;

import com.chatapp.pingchat.entity.User;
import com.chatapp.pingchat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<String> getAllUsernames(@RequestParam(required = false) String exclude) {
        return userRepository.findAll().stream()
                .map(User::getUsername)
                .filter(username -> exclude == null || !username.equals(exclude))
                .collect(Collectors.toList());
    }
}