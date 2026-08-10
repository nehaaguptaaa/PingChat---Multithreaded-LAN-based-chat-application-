package com.chatapp.pingchat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    private String id;

    private String type;       // PRIVATE or BROADCAST
    private String sender;
    private String receiver;   // PRIVATE ke liye; BROADCAST me null
    private String content;
    private long timestamp;
}