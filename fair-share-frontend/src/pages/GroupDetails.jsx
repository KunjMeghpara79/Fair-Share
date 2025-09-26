import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import api from "../api/axios";

export default function GroupDetails({ selectedGroup, onBack }) {
  const [groupData, setGroupData] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingMembers, setLoadingMembers] = useState(false);

  useEffect(() => {
    if (!selectedGroup) return;


    const fetchTransactions = async () => {
      setLoading(true);
      try {
        const response = await api.post(
          "/Groups/Get-Transactions",
          selectedGroup.code,
          {
            headers: {
              "Content-Type": "text/plain",
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }
        );
        setGroupData(response.data);
        setTransactions(response.data.transactions || []);
      } catch (error) {
        console.error("Failed to load transactions:", error);
        toast.error(error.response?.data?.message || "Failed to load transactions");
      } finally {
        setLoading(false);
      }
    };

    const fetchMembers = async () => {
      
      
      // if (!selectedGroup?.members?.length) return;



      setLoadingMembers(true);
      try {
        const fetchedMembers = await Promise.all(
          selectedGroup.members.map(async (id) => {
            try {
              
              
              const res = await api.post(
                "/get-namebyid",
                { id: id.toString() },
                {
                  headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                  },
                }
              );
            
              
              return { name: res.data };
            } catch (err) {
              console.error(`Failed to fetch member ${id}:`, err);
              return { name: `Unknown (${id})` };
            }
          })
        );
        setMembers(fetchedMembers);
      } catch (error) {
        console.error("Failed to load members:", error);
        toast.error("Failed to load group members");
      } finally {
        setLoadingMembers(false);
      }
    };

    fetchTransactions();
    fetchMembers();
  }, [selectedGroup]);

  if (!selectedGroup) {
    return (
      <div className="w-full max-w-4xl mx-auto mt-10 px-4">
        <button
          onClick={onBack}
          className="mb-6 px-4 py-2 bg-gray-200 rounded-lg hover:bg-gray-300 cursor-pointer"
        >
          ← Back to Groups
        </button>
        <h1 className="text-3xl font-bold mb-2">No Group Selected</h1>
        <p className="text-gray-600">Please select a group to view details.</p>
      </div>
    );
  }

  return (
    <div className="w-full max-w-4xl mx-auto mt-10 px-4 flex flex-col gap-6">
      <button
        onClick={onBack}
        className="cursor-pointer self-start px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
      >
        ← Back to Groups
      </button>

      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
        <h1 className="text-3xl font-bold">{groupData?.name || selectedGroup.name}</h1>
        <p className="text-gray-600 text-sm sm:text-base">
          <b> Code: </b>
          <span className="font-mono">{groupData?.code || selectedGroup.code}</span>
        </p>
      </div>

      <p className="text-gray-600">
        <b>Group Description: </b>
        {selectedGroup.description || "No description available."}
      </p>

      {/* Members Section */}
      <div className="bg-white rounded-xl shadow p-6 flex flex-col gap-4">
        <h2 className="text-xl font-semibold mb-2">Group Members</h2>
        {loadingMembers ? (
          <div className="flex justify-center items-center py-6">
            <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : members.length > 0 ? (
          <ul className="list-disc pl-6 text-gray-700">
            {members.map((m, i) => (
              <li key={i}>{m.name}</li>
            ))}
          </ul>
        ) : (
          <p className="text-gray-500">No members found.</p>
        )}
      </div>

      {/* Transactions Section */}
      <div className="bg-white rounded-xl shadow p-6 flex flex-col gap-4">
        <h2 className="text-xl font-semibold mb-2">Final Settlements</h2>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-10 gap-2">
            <div className="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
            <p className="text-gray-500 text-sm">Loading transactions...</p>
          </div>
        ) : transactions.length > 0 ? (
          transactions.map((txn, index) => (
            <div key={index} className="text-gray-700 flex items-center gap-2 border-b last:border-none pb-2">
              <span className="font-medium">{txn.fromUser.split(" ")[0]}</span>
              <span className="text-blue-500 font-bold">→</span>
              <span className="font-medium">{txn.toUser.split(" ")[0]}</span>
              <span className="ml-auto font-semibold text-green-600">₹{txn.amount.toFixed(2)}</span>
            </div>
          ))
        ) : (
          <div className="text-gray-500">No transactions yet.</div>
        )}
      </div>
    </div>
  );
}
