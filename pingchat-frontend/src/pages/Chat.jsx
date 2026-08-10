import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { WS_BASE_URL } from "../config";

function Chat() {
  const [user, setUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [selectedChat, setSelectedChat] = useState({ type: "BROADCAST" });
  const [allUsers, setAllUsers] = useState([]);
  const [onlineUsers, setOnlineUsers] = useState(new Set());
  const [search, setSearch] = useState("");
  const ws = useRef(null);
  const bottomRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const stored = localStorage.getItem("user");
    if (!stored) {
      navigate("/login");
      return;
    }
    const parsedUser = JSON.parse(stored);
    setUser(parsedUser);

    api.get("/messages/broadcast").then((res) => setMessages(res.data));
    api.get(`/users?exclude=${parsedUser.username}`).then((res) => setAllUsers(res.data));

    const socket = new WebSocket(`${WS_BASE_URL}/chat?username=${parsedUser.username}`);

    socket.onopen = () => console.log("✅ Connected to chat server");

    socket.onmessage = (event) => {
      const msg = JSON.parse(event.data);

      if (msg.type === "PRESENCE_LIST") {
        const list = msg.content ? msg.content.split(",").filter(Boolean) : [];
        setOnlineUsers(new Set(list));
        return;
      }

      if (msg.type === "PRESENCE") {
        setOnlineUsers((prev) => {
          const updated = new Set(prev);
          if (msg.content === "online") updated.add(msg.sender);
          else updated.delete(msg.sender);
          return updated;
        });
        return;
      }

      setMessages((prev) => [...prev, msg]);
    };

    socket.onerror = (err) => console.log("❌ WebSocket error", err);
    socket.onclose = () => console.log("🔴 WebSocket closed");

    ws.current = socket;
    return () => socket.close();
  }, [navigate]);

  useEffect(() => {
    if (!user) return;
    if (selectedChat.type === "BROADCAST") {
      api.get("/messages/broadcast").then((res) => setMessages(res.data));
    } else if (selectedChat.type === "PRIVATE") {
      api
        .get(`/messages/private?user1=${user.username}&user2=${selectedChat.username}`)
        .then((res) => setMessages(res.data));
    }
  }, [selectedChat, user]);

  // Auto-scroll to latest message
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, selectedChat]);

  const sendMessage = () => {
    if (!input.trim()) return;
    const payload = {
      type: selectedChat.type,
      sender: user.username,
      receiver: selectedChat.type === "PRIVATE" ? selectedChat.username : null,
      content: input,
    };
    ws.current.send(JSON.stringify(payload));
    setInput("");
  };

  const getVisibleMessages = () => {
    if (selectedChat.type === "BROADCAST") {
      return messages.filter((msg) => msg.type === "BROADCAST");
    }
    return messages.filter(
      (msg) =>
        msg.type === "PRIVATE" &&
        ((msg.sender === user.username && msg.receiver === selectedChat.username) ||
          (msg.sender === selectedChat.username && msg.receiver === user.username))
    );
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return "";
    return new Date(timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  const filteredUsers = allUsers.filter((u) => u.toLowerCase().includes(search.toLowerCase()));

  if (!user) return null;

  return (
    <div className="h-screen flex bg-[#0B1120]">
      {/* Sidebar */}
      <div className="w-80 bg-[#141B2D] border-r border-white/5 flex flex-col">
        <div className="p-4 border-b border-white/5 flex items-center gap-3">
          <div className="relative">
            <div className="w-10 h-10 rounded-full bg-[#22D3B8] flex items-center justify-center text-[#0B1120] font-bold">
              {user.username.charAt(0).toUpperCase()}
            </div>
            <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-[#22D3B8] rounded-full border-2 border-[#141B2D] animate-pulse" />
          </div>
          <div>
            <p className="text-[#E7ECF5] font-semibold text-sm">{user.username}</p>
            <p className="text-[#7C879E] text-xs uppercase tracking-wide">Online</p>
          </div>
        </div>

        <div className="p-3 border-b border-white/5">
          <input
            type="text"
            placeholder="Search users..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full p-2 rounded-lg bg-[#0B1120] text-[#E7ECF5] text-sm outline-none border border-white/5 focus:border-[#22D3B8] transition placeholder:text-[#7C879E]"
          />
        </div>

        <div className="flex-1 overflow-y-auto">
          <div
            onClick={() => setSelectedChat({ type: "BROADCAST" })}
            className={`relative p-3 flex items-center gap-3 cursor-pointer transition ${
              selectedChat.type === "BROADCAST" ? "bg-[#1C2540]" : "hover:bg-[#1C2540]/50"
            }`}
          >
            <span className="absolute left-0 top-0 h-full w-1 bg-[#F59E0B]" />
            <div className="w-10 h-10 rounded-full bg-[#F59E0B]/15 border border-[#F59E0B]/30 flex items-center justify-center text-[#F59E0B]">
              📢
            </div>
            <div>
              <p className="text-[#E7ECF5] font-medium text-sm">Broadcast</p>
              <p className="text-[#7C879E] text-xs">Everyone on the network</p>
            </div>
          </div>

          <div className="px-3 pt-3 pb-1">
            <p className="text-[#7C879E] text-xs uppercase tracking-wide">Direct messages</p>
          </div>

          {filteredUsers.map((username) => (
            <div
              key={username}
              onClick={() => setSelectedChat({ type: "PRIVATE", username })}
              className={`p-3 flex items-center gap-3 cursor-pointer transition ${
                selectedChat.type === "PRIVATE" && selectedChat.username === username
                  ? "bg-[#1C2540]"
                  : "hover:bg-[#1C2540]/50"
              }`}
            >
              <div className="relative">
                <div className="w-10 h-10 rounded-full bg-[#1C2540] border border-white/5 flex items-center justify-center text-[#E7ECF5] font-medium">
                  {username.charAt(0).toUpperCase()}
                </div>
                {onlineUsers.has(username) && (
                  <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-[#22D3B8] rounded-full border-2 border-[#141B2D]" />
                )}
              </div>
              <div>
                <p className="text-[#E7ECF5] text-sm">{username}</p>
                <p className="text-[#7C879E] text-xs">
                  {onlineUsers.has(username) ? "Online" : "Offline"}
                </p>
              </div>
            </div>
          ))}
        </div>

        <div className="p-3 border-t border-white/5 flex justify-end">
          <button
            onClick={handleLogout}
            className="text-[#7C879E] hover:text-red-400 text-sm transition"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Chat window */}
      <div className="flex-1 flex flex-col bg-[#0B1120]">
        <div className="p-4 border-b border-white/5">
          <h1 className="text-[#E7ECF5] font-semibold">
            {selectedChat.type === "BROADCAST" ? "📢 Broadcast" : selectedChat.username}
          </h1>
          {selectedChat.type === "PRIVATE" && (
            <p className="text-[#7C879E] text-xs">
              {onlineUsers.has(selectedChat.username) ? "Online" : "Offline"}
            </p>
          )}
        </div>

        <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-2">
          {getVisibleMessages().map((msg, idx) => (
            <div
              key={idx}
              className={`max-w-xs p-2.5 rounded-xl text-sm ${
                msg.sender === user.username
                  ? "bg-[#22D3B8] text-[#0B1120] self-end"
                  : "bg-[#1C2540] text-[#E7ECF5] self-start"
              }`}
            >
              {selectedChat.type === "BROADCAST" && (
                <p className="text-xs opacity-70 mb-1">{msg.sender}</p>
              )}
              <p>{msg.content}</p>
              <p className="text-[10px] opacity-60 mt-1 text-right">{formatTime(msg.timestamp)}</p>
            </div>
          ))}
          <div ref={bottomRef} />
        </div>

        <div className="p-3 border-t border-white/5 flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            placeholder="Type a message..."
            className="flex-1 p-2.5 rounded-lg bg-[#141B2D] text-[#E7ECF5] outline-none border border-white/5 focus:border-[#22D3B8] transition placeholder:text-[#7C879E]"
          />
          <button
            onClick={sendMessage}
            className="bg-[#22D3B8] hover:bg-[#1BB8A0] text-[#0B1120] font-semibold px-5 rounded-lg transition"
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
}

export default Chat;