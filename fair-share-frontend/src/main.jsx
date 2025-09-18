import React, { useEffect } from "react";
import ReactDOM from "react-dom/client";
import log from "../src/assets/kunj.png";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  useNavigate
} from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import Login from "./auth/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import "./index.css";
import Secure from "./Secure.jsx";

function AppRoutes() {
  const token = localStorage.getItem("token");
 
  useEffect(() => {
    const favicon = document.querySelector("link[rel='icon']");
    if (favicon) favicon.href = log;
   
  });

  return (
    <Routes>
      {/* Root route → check token */}
      <Route path="/" element={<Login />}/>

      {/* Dashboard route */}
      <Route path="/dashboard" element={
        <Secure>
        <Dashboard />
        </Secure>
} />

      {/* Fallback route */}
      {/* <Route path="*" element={<Navigate to="/" replace />} /> */}
    </Routes>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <GoogleOAuthProvider clientId="649806980196-nhcs98ihen9g0dukvmsvu639u66tmd38.apps.googleusercontent.com">
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </GoogleOAuthProvider>
  </React.StrictMode>
);
