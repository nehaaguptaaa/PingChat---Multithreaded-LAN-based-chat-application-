package com.chatapp.pingchat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String type;       // "PRIVATE" or "BROADCAST"
    private String sender;
    private String receiver;   // sirf PRIVATE ke liye use hoga, BROADCAST me null/ignore
    private String content;
    private long timestamp;
}