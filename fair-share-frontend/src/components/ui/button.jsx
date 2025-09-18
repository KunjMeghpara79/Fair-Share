export function Button({ children, onClick, variant = "primary", ...props }) {
  const base = "px-4 py-2 rounded-lg font-medium transition";
  const styles =
    variant === "secondary"
      ? "bg-gray-200 text-gray-800 hover:bg-gray-300"
      : "bg-blue-600 text-white hover:bg-blue-700";

  return (
    <button onClick={onClick} className={`${base} ${styles}`} {...props}>
      {children}
    </button>
  );
}
