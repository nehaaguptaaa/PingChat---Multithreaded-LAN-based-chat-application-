package com.chatapp.pingchat.controller;

import com.chatapp.pingchat.entity.Message;
import com.chatapp.pingchat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/private")
    public List<Message> getPrivateHistory(@RequestParam String user1, @RequestParam String user2) {
        return messageRepository.findBySenderAndReceiverOrSenderAndReceiver(user1, user2, user2, user1);
    }

    @GetMapping("/broadcast")
    public List<Message> getBroadcastHistory() {
        return messageRepository.findByType("BROADCAST");
    }
}