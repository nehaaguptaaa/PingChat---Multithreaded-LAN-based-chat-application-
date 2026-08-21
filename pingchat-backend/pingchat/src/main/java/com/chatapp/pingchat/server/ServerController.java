package com.chatapp.pingchat.server;

import com.chatapp.pingchat.websocket.ChatWebSocketHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/server")
public class ServerController {

    private final ServerStatus serverStatus;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ServerController(ServerStatus serverStatus, ChatWebSocketHandler chatWebSocketHandler) {
        this.serverStatus = serverStatus;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @GetMapping("/status")
    public boolean getStatus() {
        return serverStatus.isRunning();
    }

    @PostMapping("/start")
    public String startServer() {
        serverStatus.setRunning(true);
        return "Server started";
    }

    @PostMapping("/stop")
    public String stopServer() {

        serverStatus.setRunning(false);

        chatWebSocketHandler.closeAllSessions();

        return "Server stopped";
    }
}