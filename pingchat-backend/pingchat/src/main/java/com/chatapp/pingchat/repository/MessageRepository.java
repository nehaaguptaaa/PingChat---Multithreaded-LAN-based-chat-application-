package com.chatapp.pingchat.repository;

import com.chatapp.pingchat.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    // Private chat: dono directions (A->B aur B->A) ka combined history
    List<Message> findBySenderAndReceiverOrSenderAndReceiver(
            String sender1, String receiver1, String sender2, String receiver2);

    // Broadcast history
    List<Message> findByType(String type);
}