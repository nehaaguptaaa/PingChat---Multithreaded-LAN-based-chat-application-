import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/axios";

function Register() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const validate = () => {
    if (!/^[a-z_]{4,20}$/.test(username)) {
      return "Username must be 4-20 characters, lowercase letters and underscores only";
    }
    if (password.length < 8 || !/[0-9]/.test(password)) {
      return "Password must be at least 8 characters and contain a number";
    }
    if (password !== confirmPassword) {
      return "Passwords do not match";
    }
    return "";
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }
    try {
      await api.post("/auth/register", { username, password });
      navigate("/login");
    } catch (err) {
      setError(err.response?.data || "Registration failed");
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
          onSubmit={handleRegister}
          className="bg-[#141B2D] p-8 rounded-2xl shadow-xl flex flex-col gap-4 border border-white/5"
        >
          <div>
            <h1 className="text-[#E7ECF5] text-xl font-semibold">Create account</h1>
            <p className="text-[#7C879E] text-sm mt-1">Join the local network</p>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[#7C879E] text-xs uppercase tracking-wide">Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value.toLowerCase())}
              className="p-2.5 rounded-lg bg-[#0B1120] text-[#E7ECF5] outline-none border border-white/5 focus:border-[#22D3B8] transition"
              required
            />
            <p className="text-[#7C879E] text-xs">4–20 characters, lowercase letters and underscores only</p>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[#7C879E] text-xs uppercase tracking-wide">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="p-2.5 rounded-lg bg-[#0B1120] text-[#E7ECF5] outline-none border border-white/5 focus:border-[#22D3B8] transition"
              required
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[#7C879E] text-xs uppercase tracking-wide">Confirm password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="p-2.5 rounded-lg bg-[#0B1120] text-[#E7ECF5] outline-none border border-white/5 focus:border-[#22D3B8] transition"
              required
            />
          </div>

          {error && <p className="text-red-400 text-sm">{String(error)}</p>}

          <button
            type="submit"
            className="bg-[#22D3B8] hover:bg-[#1BB8A0] text-[#0B1120] font-semibold py-2.5 rounded-lg transition mt-2"
          >
            Create account
          </button>

          <p className="text-[#7C879E] text-sm text-center">
            Already have an account?{" "}
            <Link to="/login" className="text-[#22D3B8] hover:underline">
              Log in
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}

export default Register;