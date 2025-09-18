import { Navigate } from "react-router-dom";

function Secure({ children }) {
  const token = localStorage.getItem("token");

  return token ? children : <Navigate to="/" replace />;
}

export default Secure;
