package com.chatapp.pingchat.websocket;

import com.chatapp.pingchat.dto.ChatMessage;
import com.chatapp.pingchat.entity.Message;
import com.chatapp.pingchat.repository.MessageRepository;
import com.chatapp.pingchat.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

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
        logger.info("Component=WebSocket | Event=CONNECT | User={} | Thread={}", username, Thread.currentThread().getName());

        var userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            logger.error("Component=WebSocket | Event=CONNECT_FAILED | User={} | Msg=\"Unknown username attempted connection\"", username);
            return;
        }

        var user = userOptional.get();
        user.setOnline(true);
        userRepository.save(user);

        String onlineList = String.join(",", activeSessions.keySet());
        ChatMessage listMsg = new ChatMessage("PRESENCE_LIST", null, null, onlineList, System.currentTimeMillis());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(listMsg)));

        broadcastPresence(username, "online");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            chatMessage.setTimestamp(System.currentTimeMillis());

            Message toSave = new Message(null, chatMessage.getType(), chatMessage.getSender(),
                    chatMessage.getReceiver(), chatMessage.getContent(), chatMessage.getTimestamp());
            messageRepository.save(toSave);

            if ("PRIVATE".equals(chatMessage.getType())) {
                sendPrivateMessage(chatMessage);
            } else if ("BROADCAST".equals(chatMessage.getType())) {
                broadcastMessage(chatMessage);
            }
        } catch (Exception e) {
            logger.error("Component=WebSocket | Event=MESSAGE_HANDLE_FAILED | SessionId={} | Msg=\"Failed to process incoming message\" | {}",
                    session.getId(), formatException(e));
        }
    }

    private void sendPrivateMessage(ChatMessage chatMessage) {
        try {
            WebSocketSession receiverSession = activeSessions.get(chatMessage.getReceiver());
            String payload = objectMapper.writeValueAsString(chatMessage);

            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(payload));
            }

            WebSocketSession senderSession = activeSessions.get(chatMessage.getSender());
            if (senderSession != null && senderSession.isOpen()) {
                senderSession.sendMessage(new TextMessage(payload));
            }
        } catch (Exception e) {
            logger.error("Component=WebSocket | Event=PRIVATE_SEND_FAILED | From={} | To={} | Msg=\"Failed to deliver private message\" | {}",
                    chatMessage.getSender(), chatMessage.getReceiver(), formatException(e));
        }
    }

    private void broadcastMessage(ChatMessage chatMessage) {
        try {
            String payload = objectMapper.writeValueAsString(chatMessage);
            for (WebSocketSession s : activeSessions.values()) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(payload));
                }
            }
        } catch (Exception e) {
            logger.error("Component=WebSocket | Event=BROADCAST_FAILED | From={} | Msg=\"Failed to broadcast message\" | {}",
                    chatMessage.getSender(), formatException(e));
        }
    }

    private void broadcastPresence(String username, String status) {
        try {
            ChatMessage presenceMsg = new ChatMessage("PRESENCE", username, null, status, System.currentTimeMillis());
            String payload = objectMapper.writeValueAsString(presenceMsg);
            for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
                if (!entry.getKey().equals(username) && entry.getValue().isOpen()) {
                    entry.getValue().sendMessage(new TextMessage(payload));
                }
            }
        } catch (Exception e) {
            logger.error("Component=WebSocket | Event=PRESENCE_BROADCAST_FAILED | User={} | Msg=\"Failed to broadcast presence\" | {}",
                    username, formatException(e));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = getUsername(session);
        activeSessions.remove(username);

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnline(false);
            userRepository.save(user);
        });

        logger.info("Component=WebSocket | Event=DISCONNECT | User={} | Reason={}", username, status);
        broadcastPresence(username, "offline");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("Component=WebSocket | Event=TRANSPORT_ERROR | SessionId={} | Msg=\"Transport-level error\" | {}",
                session.getId(), formatException(exception));
    }

    private String getUsername(WebSocketSession session) {
        String query = session.getUri().getQuery();
        return query.replace("username=", "");
    }

    private String formatException(Throwable e) {
        String exceptionName = e.getClass().getSimpleName();
        String at = "unknown";
        if (e.getStackTrace().length > 0) {
            StackTraceElement el = e.getStackTrace()[0];
            at = el.getClassName() + "." + el.getMethodName() + "(" + el.getFileName() + ":" + el.getLineNumber() + ")";
        }
        return String.format("Exception=%s | At=%s", exceptionName, at);
    }
}