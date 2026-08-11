# PingChat — LAN-Based Multithreaded Chat Application

A real-time chat application built for devices on the same local network (WiFi/LAN), featuring one-to-one conversations and a broadcast group chat, with a WhatsApp-inspired interface.

## Features

- User registration and login (BCrypt password hashing)
- One-to-one private conversations
- Broadcast/group chat, always pinned at the top of the chat list
- Real-time messaging via WebSockets
- Online/offline status indicators
- Auto-scroll and message timestamps
- Sidebar with logged-in user's details, user search, and logout
- LAN-ready configuration (IP-based, not localhost-only) so devices on the same network can connect to each other
- Input validation on registration:
  - Username: lowercase letters and underscores only, 4–20 characters
  - Password: minimum 8 characters, at least 1 number, with confirm-password check
- Daily-rotated error logging (new `.log` file created per day)

## Tech Stack

**Backend:** Spring Boot, WebSockets, MongoDB, Java 21, Lombok

**Frontend:** React, Tailwind CSS

**Core Concepts:** Multithreading (thread-per-client handling for concurrent connections), WebSocket-based real-time messaging

## Project Structure

```
com.chatapp.pingchat
├── model/          # User, ChatMessage, Conversation
├── controller/      # Auth, chat REST endpoints
├── websocket/        # WebSocket config & handlers, per-user routing
├── service/
├── repository/       # MongoDB repositories
└── logging/          # Date-based rotating error logs
```

## How It Works

- Each connected client is handled independently, enabling multiple simultaneous conversations without blocking.
- WebSockets provide real-time, bidirectional communication between the server and connected clients for instant message delivery (both broadcast and private).
- User presence (online/offline) is tracked and pushed to clients in real time.
- The app is configured to run over LAN using the host machine's IP address, so other devices on the same network can connect without needing localhost.

## Getting Started

### Backend
```bash
cd pingchat-backend
mvn spring-boot:run
```
Configure your MongoDB connection string in `application.properties`.

### Frontend
```bash
cd pingchat-frontend
npm install
npm run dev
```
Update the WebSocket/API base URL to your machine's local IP address so other devices on the same network can connect.

## Notes

Built as a learning project to understand multithreading and real-time communication concepts relevant to backend interviews, alongside a polished, WhatsApp-style UI.
