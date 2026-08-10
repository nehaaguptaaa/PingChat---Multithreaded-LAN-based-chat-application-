import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/axios";

function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const res = await api.post("/auth/login", { username, password });
      localStorage.setItem("user", JSON.stringify(res.data));
      navigate("/chat");
    } catch (err) {
      setError(err.response?.data || "Login failed");
    }
  };

  return (
    <div className="min-h-screen bg-[#0B1120] flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="flex items-center justify-center gap-2 mb-8">
          <div className="w-9 h-9 rounded-lg bg-[#22D3B8] flex items-center justify-center">
            <span className="text-[#0B1120] font-bold text-lg">P</span>
          </div>
          <span className="text-[#E7ECF5] font-semibold text-xl tracking-tight">PingChat</span>
        </div>

        <form
          onSubmit={handleLogin}
          className="bg-[#141B2D] p-8 rounded-2xl shadow-xl flex flex-col gap-4 border border-white/5"
        >
          <div>
            <h1 className="text-[#E7ECF5] text-xl font-semibold">Welcome back</h1>
            <p className="text-[#7C879E] text-sm mt-1">Log in to your local network</p>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[#7C879E] text-xs uppercase tracking-wide">Username</label>
            <input
              type="text"
              value={username}
              maxLength={20}
              onChange={(e) => setUsername(e.target.value)}
              className="p-2.5 rounded-lg bg-[#0B1120] text-[#E7ECF5] outline-none border border-white/5 focus:border-[#22D3B8] transition"
              required
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[#7C879E] text-xs uppercase tracking-wide">Password</label>
            <input
              type="password"
              value={password}
              maxLength={25}
              onChange={(e) => setPassword(e.target.value)}
              className="p-2.5 rounded-lg bg-[#0B1120] text-[#E7ECF5] outline-none border border-white/5 focus:border-[#22D3B8] transition"
              required
            />
          </div>

          {error && <p className="text-red-400 text-sm">{String(error)}</p>}

          <button
            type="submit"
            className="bg-[#22D3B8] hover:bg-[#1BB8A0] text-[#0B1120] font-semibold py-2.5 rounded-lg transition mt-2"
          >
            Log in
          </button>

          <p className="text-[#7C879E] text-sm text-center">
            New here?{" "}
            <Link to="/register" className="text-[#22D3B8] hover:underline">
              Create an account
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}

export default Login;