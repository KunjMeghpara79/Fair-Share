import axios from "axios";
import { toast } from "react-hot-toast";

// ----------------- Create Axios Instance -----------------
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL, // Spring Boot backend
  withCredentials: true, // required if backend uses cookies
});

// ----------------- Request Interceptor -----------------
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token && token.trim() !== "") {
      config.headers = {
        ...(config.headers || {}),
        Authorization: token.startsWith("Bearer ") ? token : `Bearer ${token}`,
      };
    } else {
      // Remove Authorization header if no token
      if (config.headers?.Authorization) delete config.headers.Authorization;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ----------------- Response Interceptor -----------------
api.interceptors.response.use(
  (response) => response, // pass through successful responses
  // (error) => {
  //   if (error.response) {
  //     const status = error.response.status;
  //     const message = error.response.data?.message || error.response.data || "Something went wrong";

  //     // Handle 401 Unauthorized or invalid JWT
  //     if (status === 401 || status === 403) {
  //       // localStorage.clear(); // clear token and other storage
  //       // toast.error("Session expired or invalid token. Please login again.", { position: "top-center" });

  //       // Use window.location.href only if you want a full reload
  //       // For SPA-friendly redirect, use a callback or event to navigate via React Router
  //       // setTimeout(() => {
  //         console.log("axios 44");
          
  //         window.location.href = "/"; 
  //       // }, 500);
  //     } else {
  //       toast.error(message, { position: "top-left" });
  //     }
  //   } else {
  //     // network or unknown errors
  //     toast.error("Network error. Please try again.", { position: "top-right" });
  //   }

  //   return Promise.reject(error);
  // }
);

export default api; 

