package com.chatapp.pingchat.server;

import org.springframework.stereotype.Component;

@Component
public class ServerStatus {

    private boolean running = true;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}