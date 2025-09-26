import { useEffect, useState } from "react";
import { FaUsers, FaPlus, FaSignOutAlt, FaSearch } from "react-icons/fa";
import { FiCopy } from "react-icons/fi";
import { motion, AnimatePresence } from "framer-motion";
import api from "../api/axios";
import toast, { Toaster } from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import GroupDetails from "./GroupDetails";

export default function Dashboard() {
  const navigate = useNavigate();
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);

  const [searchQuery, setSearchQuery] = useState("");

  // Join group states
  const [joinLoading, setJoinLoading] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [groupCodeInput, setGroupCodeInput] = useState("");
  const [instate, setInstate] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState();

  // Create group states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [newGroup, setNewGroup] = useState({ name: "", description: "" });

  const token = localStorage.getItem("token");

  // ------------------- FETCH GROUPS -------------------
  const fetchGroups = async () => {
    setLoading(true);
    try {
      const res = await api.get("/Groups/Get-Groups", {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (Array.isArray(res.data)) setGroups(res.data);
      else setGroups([]);
    } catch (err) {
      console.error("Failed to fetch groups:", err);
      toast.error("Failed to load groups", { position: "top-center" });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) return navigate("/"); // redirect if no token
    document.title = "FairShare - Dashboard";
    fetchGroups();
  }, []);

  // ------------------- LOGOUT -------------------
  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("name");
    navigate("/");
  };

  // ------------------- JOIN GROUP -------------------
  const handleJoinGroup = async () => {
    if (!groupCodeInput) return;
    setJoinLoading(true);
    try {
      await api.post("/Groups/Join", groupCodeInput, {
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "text/plain" },
      });
      toast.success("Joined group successfully!", { position: "top-center" });
      setShowJoinModal(false);
      setGroupCodeInput("");
      fetchGroups();
    } catch (err) {
      const errorMessage = err.response?.data || "Something went wrong";
      toast.error(errorMessage, { position: "top-center" });
    } finally {
      setJoinLoading(false);
    }
  };

  // ------------------- CREATE GROUP -------------------
  const handleCreateGroup = async () => {
    if (!newGroup.name.trim() || !newGroup.description.trim()) {
      toast.error("Both name and description are required", { position: "top-center" });
      return;
    }
    setCreateLoading(true);
    try {
      await api.post("/Groups/Create", newGroup, {
        headers: { Authorization: `Bearer ${token}` },
      });
      toast.success("Group created successfully!", { position: "top-center" });
      setShowCreateModal(false);
      setNewGroup({ name: "", description: "" });
      fetchGroups();
    } catch (err) {
      const errorMessage = err.response?.data || "Failed to create group";
      toast.error(errorMessage, { position: "top-center" });
    } finally {
      setCreateLoading(false);
    }
  };

  // ------------------- GROUP CARD -------------------
  function GroupCard({ name, members, code, createdAt }) {
    const handleCopy = () => navigator.clipboard.writeText(code);
    const formattedDate = createdAt ? new Date(createdAt).toLocaleDateString() : "N/A";

    return (
      <div className="relative bg-white/80 backdrop-blur-lg border border-blue-200 rounded-2xl shadow-lg p-6 w-full sm:w-80 hover:shadow-xl transition-all flex flex-col gap-4">
        <div className="flex justify-between items-start">
          <h2 className="text-xl font-semibold text-gray-800">{name}</h2>
          <button onClick={handleCopy} className="text-gray-400 hover:text-gray-600 transition cursor-pointer">
            <FiCopy size={18} />
          </button>
        </div>
        <p className="text-gray-500 flex items-center gap-2">
          <FaUsers className="text-gray-400" size={15} />
          <span className="font-medium">{members}</span> members
        </p>
        <div className="flex justify-between text-gray-400 text-sm">
          <span>Code: {code}</span>
          <span>Created on {formattedDate}</span>
        </div>
        <button
          onClick={async () => {
            setInstate(true);
            try {
              const res = await api.post(
                "/Groups/Get-Groupbycode",
                code,
                {
                  headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "text/plain",
                  },
                }
              );
              //console.log(res.data);

              setSelectedGroup({ name: res.data.name, members: res.data.members, code: res.data.groupcode, description: res.data.description });
              //console.log(selectedGroup);

            } catch (err) {
              console.error("Failed to fetch group:", err);
              toast.error("Failed to load group", { position: "top-center" });
            }
          }}
          className="mt-4 w-full bg-gradient-to-r from-blue-500 to-blue-400 text-white py-2 rounded-lg font-semibold transition-all shadow-md hover:shadow-xl hover:scale-105 cursor-pointer"
        >
          ₹ View Expenses
        </button>
      </div>
    );
  }

  // ------------------- FILTERED GROUPS -------------------
  const filteredGroups = groups.filter(group =>
    group.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="h-screen bg-gradient-to-br from-blue-50 via-blue-100 to-blue-50 flex flex-col font-inter antialiased">
      <Toaster />
      {/* NAVBAR */}
      <nav className="bg-white/70 backdrop-blur-lg border-b border-blue-100 shadow-lg px-4 sm:px-8 py-3 flex flex-wrap justify-between items-center sticky top-0 z-20 rounded-b-2xl gap-3">
        <motion.h1
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          className="text-2xl sm:text-3xl font-extrabold bg-gradient-to-r from-blue-700 to-blue-400 bg-clip-text text-transparent hover:scale-105 transition flex-shrink-0"
        >
          FairShare
        </motion.h1>

        <div className="flex flex-1 flex-wrap sm:flex-nowrap items-center gap-2 sm:gap-6 justify-end">
          <div className="flex items-center bg-blue-100 px-3 py-2 rounded-2xl gap-2 flex-1 sm:flex-none min-w-[20px]">
            <FaSearch className="text-gray-600" />
            <input
              type="text"
              placeholder="Search groups..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="bg-transparent focus:outline-none w-full text-sm sm:text-base"
            />
          </div>

          <motion.button whileHover={{ scale: 1.05 }} onClick={handleLogout} className="flex h-9 items-center gap-2 px-4 sm:px-5 py-2.5 rounded-2xl font-bold text-red-700 bg-red-100 hover:bg-red-200 transition cursor-pointer flex-shrink-0">
            <FaSignOutAlt /> Logout
          </motion.button>
        </div>
      </nav>

      {/* MAIN CONTENT */}
      <main className="flex-1 px-4 sm:px-8 py-8 sm:py-14 flex flex-col overflow-y-auto items-center">
        {instate && selectedGroup ? (
          <GroupDetails
            selectedGroup={selectedGroup}
            onBack={() => {
              setSelectedGroup(null);
              setInstate(false);
            }}
          />
        ) : (
          <motion.div
            initial={{ opacity: 0, y: -40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7 }}
            className="w-full max-w-screen-xl"
          >
            {/* Header & Actions */}
            <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center mb-4 gap-4 sm:gap-8">
              <div className="max-w-2xl">
                <h2 className="text-3xl sm:text-2xl font-extrabold text-gray-900 leading-snug">
                  Welcome {localStorage.getItem("name")?.split(" ")[0]}!
                </h2>
                <h2 className="text-3xl sm:text-5xl font-extrabold text-gray-900 leading-snug">
                  Your Groups
                </h2>
                <p className="text-gray-600 text-base sm:text-lg mt-2 sm:mt-4 leading-relaxed">
                  Manage shared expenses with clarity, fairness, and zero stress.
                </p>
              </div>
              <div className="flex gap-3 sm:gap-5 flex-wrap">
                <motion.button
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  onClick={() => setShowJoinModal(true)}
                  className="flex items-center gap-2 px-4 sm:px-6 py-2 sm:py-3 bg-gradient-to-r from-blue-500 to-blue-400 text-white font-bold rounded-2xl shadow-md hover:shadow-xl transition cursor-pointer"
                >
                  <FaUsers /> Join Group
                </motion.button>

                <motion.button
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  onClick={() => setShowCreateModal(true)}
                  className="flex items-center gap-2 px-4 sm:px-6 py-2 sm:py-3 bg-gradient-to-r from-blue-400 to-blue-500 text-white font-bold rounded-2xl shadow-md hover:shadow-xl transition cursor-pointer"
                >
                  <FaPlus /> Create Group
                </motion.button>
              </div>
            </div>
            <br />
            {/* GROUPS GRID */}
            <AnimatePresence mode="wait">
              {loading ? (
                <div className="flex flex-col justify-center items-center w-full min-h-[60vh] mt-20">
                  <motion.div
                    animate={{ rotate: 360 }}
                    transition={{ duration: 1.2, repeat: Infinity, ease: "linear" }}
                    className="w-14 h-14 border-4 border-blue-600 border-t-transparent rounded-full mb-6"
                  />
                  <p className="text-gray-600 font-semibold">Loading your groups...</p>
                </div>
              ) : filteredGroups.length === 0 ? (
                <div className="flex flex-col justify-center items-center w-full min-h-[60vh]">
                  <p className="text-gray-600 text-center text-xl">No Groups Found.</p>
                </div>
              ) : (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 sm:gap-8 mt-6 w-full justify-center"
                >
                  {filteredGroups.map((group, idx) => (
                    <GroupCard
                      key={idx}
                      name={group.name}
                      members={group.members?.length || 0}
                      code={group.groupcode}
                      createdAt={group.createdAt}
                    />
                  ))}
                </motion.div>
              )}
            </AnimatePresence>

            {/* JOIN & CREATE MODALS */}
            {showJoinModal && (
              <Modal title="Join a Group" onClose={() => setShowJoinModal(false)}>
                <input
                  type="text"
                  placeholder="Enter group code"
                  value={groupCodeInput}
                  onChange={(e) => setGroupCodeInput(e.target.value)}
                  className="border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400 w-full"
                />
                <div className="flex justify-end gap-2 mt-4">
                  <button onClick={() => setShowJoinModal(false)} className="px-4 py-2 rounded-lg bg-gray-200 hover:bg-gray-300 transition">Cancel</button>
                  <button onClick={handleJoinGroup} disabled={joinLoading} className="px-4 py-2 rounded-lg bg-blue-500 text-white hover:bg-blue-600 transition disabled:opacity-50 disabled:cursor-not-allowed">
                    {joinLoading ? "Joining..." : "Join"}
                  </button>
                </div>
              </Modal>
            )}
            {showCreateModal && (
              <Modal title="Create a Group" onClose={() => setShowCreateModal(false)}>
                <input
                  type="text"
                  placeholder="Enter group name"
                  value={newGroup.name}
                  onChange={(e) => setNewGroup({ ...newGroup, name: e.target.value })}
                  className="border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400 w-full"
                />
                <input
                  type="text"
                  placeholder="Enter description"
                  value={newGroup.description}
                  onChange={(e) => setNewGroup({ ...newGroup, description: e.target.value })}
                  className="border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400 w-full mt-2"
                />
                <div className="flex justify-end gap-2 mt-4">
                  <button onClick={() => setShowCreateModal(false)} className="px-4 py-2 rounded-lg bg-gray-200 hover:bg-gray-300 transition">Cancel</button>
                  <button onClick={handleCreateGroup} disabled={createLoading} className="px-4 py-2 rounded-lg bg-blue-500 text-white hover:bg-blue-600 transition disabled:opacity-50 disabled:cursor-not-allowed">
                    {createLoading ? "Creating..." : "Create"}
                  </button>
                </div>
              </Modal>
            )}
          </motion.div>
        )}
      </main>
    </div>
  );
}

// Reusable Modal component
function Modal({ title, children, onClose }) {
  return (
    <div className="fixed inset-0 bg-black/40 flex justify-center items-center z-50 px-4">
      <motion.div
        initial={{ scale: 0.8, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.8, opacity: 0 }}
        className="bg-white rounded-2xl p-6 sm:p-8 w-full max-w-sm shadow-2xl flex flex-col gap-4"
      >
        <h3 className="text-2xl font-bold text-gray-800">{title}</h3>
        {children}
      </motion.div>
    </div>
  );
}
