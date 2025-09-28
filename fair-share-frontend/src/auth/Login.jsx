import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FcGoogle } from "react-icons/fc";
import { GoogleLogin } from "@react-oauth/google";
import "./Auth.css";
import "boxicons/css/boxicons.min.css";
import logo from "../assets/paisa.png";

// ---------------- DOTS LOADER ----------------
function LoadingDots() {
  const [dots, setDots] = useState("");

  useEffect(() => {
    const interval = setInterval(() => {
      setDots((prev) => (prev.length >= 4 ? "" : prev + "."));
    }, 500);
    return () => clearInterval(interval);
  }, []);

  return <span>{dots}</span>;
}

export default function Auth() {
  const navigate = useNavigate();
  const [mode, setMode] = useState("");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [message, setMessage] = useState("");

  // forgot password states
  const [forgotEmail, setForgotEmail] = useState("");
  const [showForgot, setShowForgot] = useState(false);
  const [forgotStep, setForgotStep] = useState("email"); // "email" | "otp" | "reset"
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");

  // loading state
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const token = localStorage?.getItem("token");
    if (!!token) {
      navigate("/dashboard");
    }
  }, []);

  useEffect(() => {
    document.title = "Login || Register";
    const timer = setTimeout(() => setMode("sign-in"), 200);
    return () => clearTimeout(timer);
  }, []);

  const toggleMode = () => {
    setMode((prev) => (prev === "sign-in" ? "sign-up" : "sign-in"));
    setMessage("");
  };

  // ---------------- REGISTER ----------------
  const handleRegister = async (e) => {
    e.preventDefault();
    const passwordRegex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$/;

    if (!passwordRegex.test(password)) {
      setMessage(
        "❌ Password must be at least 6 characters, include uppercase, lowercase, number, and special character."
      );
      return;
    }

    if (password !== confirmPassword) {
      setMessage("❌ Passwords do not match!");
      return;
    }

    try {
      const res = await fetch("http://localhost:8080/Register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, password }),
      });

      if (!res.ok) throw new Error("Registration failed");

      setMessage("✅ Registration successful! Please login.");
      setMode("sign-in");
    } catch {
      setMessage("❌ User already exists or invalid input");
    }
  };

  // ---------------- LOGIN ----------------
  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/Login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
        credentials: "include",
      });

      if (!res.ok) throw new Error("Login failed");

      const data = await res.json();
      if (data.error) {
        setMessage(`❌ ${data.error}`);
        return;
      }

      localStorage.setItem("token", data.token);
      localStorage.setItem("name", data.name);
      localStorage.setItem("email", data.email);
      navigate("/dashboard");
    } catch {
      setMessage("❌ Login error. Check credentials.");
    } finally {
      setLoading(false);
    }
  };

  // ---------------- GOOGLE LOGIN ----------------
  const handleGoogleLoginSuccess = async (credentialResponse) => {
    setLoading(true);
    try {
      const googleToken = credentialResponse.credential;

      const res = await fetch("http://localhost:8080/api/auth/google", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token: googleToken }),
      });

      if (!res.ok) throw new Error("Google login failed");

      const data = await res.json();

      localStorage.setItem("token", data.jwt);
      localStorage.setItem("email", data.email);
      localStorage.setItem("name", data.name);

      navigate("/dashboard");
    } catch {
      setMessage("❌ Google login failed");
    } finally {
      setLoading(false);
    }
  };

  // ---------------- FORGOT PASSWORD FLOW ----------------
  const handleForgotPassword = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: forgotEmail,
      });

      if (!res.ok) throw new Error("Failed");
      setMessage("✅ OTP sent to your email.");
      setForgotStep("otp");
    } catch {
      setMessage("❌ Failed to send reset mail.");
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/verify-otp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: forgotEmail, otp }),
      });

      if (!res.ok) throw new Error("Invalid OTP");
      setMessage("✅ OTP verified. Please enter new password.");
      setForgotStep("reset");
    } catch {
      setMessage("❌ Invalid OTP.");
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();

    const passwordRegex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$/;

    if (!passwordRegex.test(newPassword)) {
      setMessage(
        "❌ Password must be at least 6 characters, include uppercase, lowercase, number, and special character."
      );
      return; // Stop submission if invalid
    }

    setLoading(true);
    try {
      const res = await fetch("http://localhost:8080/change-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: forgotEmail, password: newPassword }),
      });

      if (!res.ok) throw new Error("Failed to change password");

      setMessage("✅ Password changed successfully. Please login.");
      setForgotStep("email");
      setShowForgot(false);
    } catch {
      setMessage("❌ Failed to change password.");
    } finally {
      setLoading(false);
    }
  };


  return (
    <div id="container" className={`container ${mode}`}>
      <div className="row">
        {/* SIGN UP */}
        <div className="col align-items-center flex-col sign-up">
          <div className="form-wrapper align-items-center">
            <form className="form sign-up" onSubmit={handleRegister}>
              {message && <p className="msg">{message}</p>}
              <div className="input-group">
                <i className="bx bxs-user"></i>
                <input
                  type="text"
                  placeholder="Full name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>
              <div className="input-group">
                <i className="bx bx-mail-send"></i>
                <input
                  type="email"
                  placeholder="Email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <div className="input-group">
                <i className="bx bxs-lock-alt"></i>
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="Password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              <div className="input-group">
                <i className="bx bxs-lock-alt"></i>
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="Confirm password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </div>
              <label className="checkbox">
                <input
                  type="checkbox"
                  checked={showPassword}
                  onChange={() => setShowPassword(!showPassword)}
                />{" "}
                Show Password
              </label>
              <button type="submit">Sign up</button>
              <p>
                <span>Already have an account? </span>
                <b onClick={toggleMode} className="pointer">
                  Sign in here
                </b>
              </p>
            </form>
          </div>
        </div>

        {/* SIGN IN */}
        <div className="col align-items-center flex-col sign-in">
          <div className="form-wrapper align-items-center">
            {!showForgot ? (
              <form className="form sign-in" onSubmit={handleLogin}>
                {message && <p className="msg">{message}</p>}
                <div className="input-group">
                  <i className="bx bx-mail-send"></i>
                  <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
                <div className="input-group">
                  <i className="bx bxs-lock-alt"></i>
                  <input
                    type={showPassword ? "text" : "password"}
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
                <label className="checkbox">
                  <input
                    type="checkbox"
                    checked={showPassword}
                    onChange={() => setShowPassword(!showPassword)}
                  />{" "}
                  Show Password
                </label>
                <button type="submit" disabled={loading}>
                  {loading ? (
                    <>
                      Processing<LoadingDots />
                    </>
                  ) : (
                    "Sign in"
                  )}
                </button>

                {/* Google login */}
                <div style={{ width: "50%", marginTop: "10px", marginLeft: "25%" }}>

                  <GoogleLogin
                    onSuccess={handleGoogleLoginSuccess}
                    onError={() => setMessage("❌ Google login failed")}
                  />

                </div>

                <p>
                  <b onClick={() => setShowForgot(true)} className="pointer">
                    Forgot password?
                  </b>
                </p>
                <p>
                  <span>Don’t have an account? </span>
                  <b onClick={toggleMode} className="pointer">
                    Register here
                  </b>
                </p>
              </form>
            ) : (
              <div className="form sign-in">
                {message && <p className="msg">{message}</p>}

                {forgotStep === "email" && (
                  <form onSubmit={handleForgotPassword}>
                    <div className="input-group">
                      <i className="bx bx-mail-send"></i>
                      <input
                        type="email"
                        placeholder="Enter your email"
                        value={forgotEmail}
                        onChange={(e) => setForgotEmail(e.target.value)}
                        required
                      />
                    </div>
                    <button type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          Sending<LoadingDots />
                        </>
                      ) : (
                        "Send OTP"
                      )}
                    </button>
                    <p>
                      <b onClick={() => setShowForgot(false)} className="pointer">
                        Back to Login
                      </b>
                    </p>
                  </form>
                )}

                {forgotStep === "otp" && (
                  <form onSubmit={handleVerifyOtp}>
                    <div className="input-group">
                      <i className="bx bxs-lock-alt"></i>
                      <input
                        type="text"
                        placeholder="Enter OTP"
                        value={otp}
                        onChange={(e) => setOtp(e.target.value)}
                        required
                      />
                    </div>
                    <button type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          Verifying<LoadingDots />
                        </>
                      ) : (
                        "Verify OTP"
                      )}
                    </button>
                    <p>
                      <b onClick={() => setForgotStep("email")} className="pointer">
                        Resend OTP
                      </b>
                    </p>
                  </form>
                )}

                {forgotStep === "reset" && (
                  <form onSubmit={handleChangePassword}>
                    <div className="input-group">
                      <i className="bx bxs-lock-alt"></i>
                      <input
                        type="password"
                        placeholder="New password"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        required
                      />
                    </div>
                    <button type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          Changing<LoadingDots />
                        </>
                      ) : (
                        "Change Password"
                      )}
                    </button>
                  </form>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* CONTENT SECTION */}
      <div className="row content-row">
        <div className="col align-items-center flex-col">
          <div className="text sign-in">
            <h2>Welcome to</h2>
            <h2>Fair-Share</h2>
          </div>
          <div className="img sign-in"></div>
        </div>

        <div className="col align-items-center flex-col">
          <div className="img sign-up"></div>
          <div className="text sign-up">
            <h2>Join with us</h2>
          </div>
        </div>
      </div>
    </div>
  );
}
