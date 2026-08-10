package com.chatapp.pingchat.websocket;

import com.chatapp.pingchat.dto.ChatMessage;
import com.chatapp.pingchat.entity.Message;
import com.chatapp.pingchat.entity.User;
import com.chatapp.pingchat.repository.MessageRepository;
import com.chatapp.pingchat.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = getUsername(session);
        activeSessions.put(username, session);

        System.out.println("Connected: " + username + " | Thread: " + Thread.currentThread().getName());

        // Mark online in DB
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnline(true);
            userRepository.save(user);
        });

        // Send the current online list to the newly connected client
        String onlineList = String.join(",", activeSessions.keySet());
        ChatMessage listMsg = new ChatMessage("PRESENCE_LIST", null, null, onlineList, System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(listMsg)));

        // Broadcast this user's "online" status to everyone else
        broadcastPresence(username, "online");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
        chatMessage.setTimestamp(System.currentTimeMillis());

        System.out.println("Handling on Thread: " + Thread.currentThread().getName()
                + " | Type: " + chatMessage.getType());

        Message toSave = new Message(null, chatMessage.getType(), chatMessage.getSender(),
                chatMessage.getReceiver(), chatMessage.getContent(), chatMessage.getTimestamp());
        messageRepository.save(toSave);

        if ("PRIVATE".equals(chatMessage.getType())) {
            sendPrivateMessage(chatMessage);
        } else if ("BROADCAST".equals(chatMessage.getType())) {
            broadcastMessage(chatMessage);
        }
    }

    private void sendPrivateMessage(ChatMessage chatMessage) throws Exception {
        WebSocketSession receiverSession = activeSessions.get(chatMessage.getReceiver());
        String payload = objectMapper.writeValueAsString(chatMessage);

        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(payload));
        }

        WebSocketSession senderSession = activeSessions.get(chatMessage.getSender());
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(payload));
        }
    }

    private void broadcastMessage(ChatMessage chatMessage) throws Exception {
        String payload = objectMapper.writeValueAsString(chatMessage);
        for (WebSocketSession s : activeSessions.values()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    private void broadcastPresence(String username, String status) throws Exception {
        ChatMessage presenceMsg = new ChatMessage("PRESENCE", username, null, status, System.currentTimeMillis());
        String payload = objectMapper.writeValueAsString(presenceMsg);
        for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
            if (!entry.getKey().equals(username) && entry.getValue().isOpen()) {
                entry.getValue().sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = getUsername(session);
        activeSessions.remove(username);

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnline(false);
            userRepository.save(user);
        });

        System.out.println("Disconnected: " + username);
        broadcastPresence(username, "offline");
    }

    private String getUsername(WebSocketSession session) {
        String query = session.getUri().getQuery();
        return query.replace("username=", "");
    }
}