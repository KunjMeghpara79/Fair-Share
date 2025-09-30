import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import api from "../api/axios";
import { AiOutlinePlus } from "react-icons/ai";


export default function GroupDetails({ selectedGroup, onBack, searchQuery }) {
  const [groupData, setGroupData] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [members, setMembers] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingMembers, setLoadingMembers] = useState(false);
  const [loadingExpenses, setLoadingExpenses] = useState(false);

  const [selectedExpense, setSelectedExpense] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [savingExpense, setSavingExpense] = useState(false);

  const [newExpense, setNewExpense] = useState({
    description: "",
    amount: "",
    currency: "INR",
    splitType: "EQUAL",
    payername: "",
    date: new Date().toISOString().slice(0, 10),
    splitDetails: [],
  });

  const filteredExpenses = expenses.filter((exp) =>
    exp.description.toLowerCase().includes(searchQuery?.toLowerCase() || "")
  );

  // Fetch final settlements
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
      toast.error(error.response?.data?.message || "Failed to load transactions");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!selectedGroup) return;

    fetchTransactions();

    const fetchMembers = async () => {
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
            } catch {
              return { name: `Unknown (${id})` };
            }
          })
        );
        setMembers(fetchedMembers);
      } catch {
        toast.error("Failed to load group members");
      } finally {
        setLoadingMembers(false);
      }
    };

    const fetchExpenses = async () => {
      setLoadingExpenses(true);
      try {
        const response = await api.get(
          `/Groups/Get-Expenses/${selectedGroup.code}`,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }
        );
        const data = Array.isArray(response.data)
          ? response.data
          : [response.data];
        setExpenses(data);
      } catch (error) {
        toast.error(error.response?.data?.message || "Failed to load expenses");
      } finally {
        setLoadingExpenses(false);
      }
    };

    fetchMembers();
    fetchExpenses();
  }, [selectedGroup]);



  // 🔹 Delete Expense
  const handleDeleteExpense = async (eid) => {
    if (!window.confirm("Are you sure you want to delete this expense?")) return;

    try {
      const response = await api.delete("/Expense/Delete", {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        data: { id: eid },
      });

      toast.success(response.data || "Expense deleted successfully!");

      // Refresh expenses
      const res = await api.get(`/Groups/Get-Expenses/${selectedGroup.code}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      setExpenses(Array.isArray(res.data) ? res.data : [res.data]);

      // Refresh final settlements
      await fetchTransactions();
    } catch (err) {
      toast.error(err.response?.data || "Failed to delete expense");
    }
  };

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
      {/* Top Buttons */}
      <div className="w-full max-w-4xl mx-auto mt-10  flex justify-start sm:justify-between items-center gap-2">
        {/* Add Expense Button */}
        <button
          onClick={() => setIsAddModalOpen(true)}
          className="flex items-center pl-2 justify-center  gap-2 px-4 py-2 sm:px-6 sm:py-3 bg-gradient-to-r from-blue-500 to-blue-400 text-white font-bold rounded-2xl shadow-md hover:shadow-xl transition cursor-pointer min-w-[140px]"
        >
          <AiOutlinePlus className="text-white" size={20} /> Add Expense
        </button>

        {/* Delete Group Button */}

      </div>



      {/* Group Info */}
      <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
        <h1 className="text-3xl font-bold">
          {groupData?.name || selectedGroup.name}
        </h1>
        <p className="text-gray-600 text-sm sm:text-base">
          <b> Code: </b>
          <span className="font-mono">
            {groupData?.code || selectedGroup.code}
          </span>
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
            <div
              key={index}
              className="text-gray-700 flex items-center gap-2 border-b last:border-none pb-2"
            >
              <span className="font-medium">{txn.fromUser.split(" ")[0]}</span>
              <span className="text-blue-500 font-bold">→</span>
              <span className="font-medium">{txn.toUser.split(" ")[0]}</span>
              <span className="ml-auto font-semibold text-green-600">
                ₹{txn.amount.toFixed(2)}
              </span>
            </div>
          ))
        ) : (
          <div className="text-gray-500">No transactions yet.</div>
        )}
      </div>

      {/* Expenses Section */}
      <div className="bg-white rounded-xl shadow p-6 flex flex-col gap-4">
        <h2 className="text-xl font-semibold mb-2">Expenses</h2>
        {loadingExpenses ? (
          <div className="flex justify-center items-center py-6">
            <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : filteredExpenses.length > 0 ? (
          <div className="grid gap-4 sm:grid-cols-2">
            {filteredExpenses.map((exp, i) => (
              <div
                key={i}
                className="border rounded-xl shadow-sm p-4 flex flex-col justify-between"
              >
                <div className="flex flex-col sm:flex-row sm:items-center mb-15 sm:justify-between gap-2">
                  <h3 className="text-lg font-semibold text-gray-900">{exp.description}</h3>
                  <div className="flex flex-wrap items-center gap-4 sm:gap-6">
                    <p className="text-red-600 font-bold">
                      ₹{exp.amount?.toFixed(2) || 0}
                    </p>
                    <p className="text-gray-600 text-sm">
                      <span className="font-medium">Added by:</span> {exp.adder}
                    </p>
                  </div>
                </div>

                <div className="flex gap-2 mt-4">
                  <button
                    onClick={() => {
                      setSelectedExpense(exp);
                      setIsModalOpen(true);
                    }}
                    className="flex-1 cursor-pointer px-3 py-1.5 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
                  >
                    View
                  </button>

                  <button
                    onClick={() => handleDeleteExpense(exp.id)}
                    className="flex-1 cursor-pointer px-3 py-1.5 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                  >
                    Delete
                  </button>
                </div>

              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500">No expenses recorded yet.</p>
        )}
      </div>

      {/* View Expense Modal */}
      {isModalOpen && selectedExpense && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4 sm:px-0">
          <div
            className="absolute inset-0 backdrop-blur-sm"
            onClick={() => setIsModalOpen(false)}
          ></div>
          <div className="relative bg-white/90 backdrop-blur-md rounded-2xl shadow-2xl p-6 sm:p-8 w-full max-w-md sm:max-w-lg z-50 border border-gray-200">
            <button
              onClick={() => setIsModalOpen(false)}
              className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 text-lg cursor-pointer"
            >
              ✖
            </button>
            <h2 className="text-xl sm:text-2xl font-bold text-gray-900 mb-4">
              <b>Description:</b> {selectedExpense.description}
            </h2>
            <div className="space-y-2 sm:space-y-3 mb-6 text-sm sm:text-base">
              <p className="text-gray-700">
                <span className="font-semibold">Payer:</span> {selectedExpense.payername}
              </p>
              <p className="text-gray-700">
                <span className="font-semibold">Amount:</span> ₹{selectedExpense.amount?.toFixed(2) || 0}
              </p>
              <p className="text-gray-700">
                <span className="font-semibold">Split Type:</span> {selectedExpense.splitType || "Equal"}
              </p>
              <p className="text-gray-700">
                <span className="font-semibold">Date:</span>{" "}
                {selectedExpense.date
                  ? new Date(selectedExpense.date).toLocaleDateString("en-IN", {
                    day: "2-digit",
                    month: "short",
                    year: "numeric",
                  })
                  : "N/A"}
              </p>
            </div>
            <br />
            <h3 className="text-base sm:text-lg font-semibold text-gray-800 mb-3">
              Split Details
            </h3>
            <div className="space-y-2">
              {selectedExpense.splitDetails?.map((sd, j) => (
                <div
                  key={j}
                  className="flex justify-between items-center bg-gray-50 p-2 sm:p-3 rounded-lg border border-gray-200"
                >
                  <span className="font-medium text-gray-700 text-sm sm:text-base">{sd.name}</span>
                  <span className="text-green-600 font-semibold text-sm sm:text-base">
                    {sd.shareAmount ? `₹${sd.shareAmount.toFixed(2)}` : `${sd.percentage}%`}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
      {/* Add Expense Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center px-4 sm:px-0">
          <div
            className="absolute inset-0 backdrop-blur-sm"
            onClick={() => setIsAddModalOpen(false)}
          ></div>

          <div className="relative bg-white/90 backdrop-blur-md rounded-2xl shadow-2xl p-6 sm:p-8 w-full max-w-md sm:max-w-lg z-50 border border-gray-200">
            {/* Close button */}
            <button
              onClick={() => setIsAddModalOpen(false)}
              className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 text-lg cursor-pointer"
            >
              ✖
            </button>

            {/* Title */}
            <h2 className="text-xl sm:text-2xl font-bold text-gray-900 mb-4">
              Add Expense
            </h2>
            <br />
            <div className="flex flex-col gap-3 w-full max-w-md mx-auto p-2 sm:p-4">
              {/* Description */}
              <input
                type="text"
                placeholder="Description"
                value={newExpense.description}
                onChange={(e) =>
                  setNewExpense({ ...newExpense, description: e.target.value })
                }
                className="border p-2 rounded-xl w-full"
              />

              {/* Amount */}
              <input
                type="number"
                placeholder="Amount"
                value={newExpense.amount}
                onChange={(e) =>
                  setNewExpense({
                    ...newExpense,
                    amount: parseFloat(e.target.value),
                  })
                }
                className="border p-2 rounded-xl w-full"
              />

              {/* Payer */}
              <select
                value={newExpense.payername}
                onChange={(e) =>
                  setNewExpense({ ...newExpense, payername: e.target.value })
                }
                className="border p-2 rounded-xl w-full"
              >
                <option value="" disabled hidden>
                  Select Payer
                </option>
                {members.map((m, idx) => (
                  <option key={idx} value={m.name}>
                    {m.name}
                  </option>
                ))}
              </select>

              {/* Date */}
              <input
                type="date"
                value={newExpense.date}
                onChange={(e) =>
                  setNewExpense({ ...newExpense, date: e.target.value })
                }
                className="border p-2 rounded-xl w-full"
              />

              {/* Split Type */}
              <select
                value={newExpense.splitType}
                onChange={(e) =>
                  setNewExpense({
                    ...newExpense,
                    splitType: e.target.value,
                    splitDetails: [],
                  })
                }
                className="border p-2 rounded-xl w-full"
              >
                <option value="EQUAL">Equal</option>
                <option value="CUSTOM">Custom</option>
                <option value="PERCENTAGE">By Percentage</option>
              </select>

              {/* Split Details */}
              {(newExpense.splitType === "CUSTOM" ||
                newExpense.splitType === "PERCENTAGE") && (
                  <div className="flex flex-col gap-2">
                    <h3 className="font-medium">Split Details</h3>

                    {newExpense.splitDetails.map((sd, idx) => (
                      <div
                        key={idx}
                        className="flex flex-col sm:flex-row gap-2 items-center"
                      >
                        {/* Name Dropdown */}
                        <select
                          value={sd.name}
                          onChange={(e) => {
                            const updated = [...newExpense.splitDetails];
                            updated[idx].name = e.target.value;
                            setNewExpense({ ...newExpense, splitDetails: updated });
                          }}
                          className="border p-2 rounded-xl flex-1 w-full sm:w-auto"
                        >
                          <option value="">Select Member</option>
                          {members
                            .filter(
                              (m) =>
                                !newExpense.splitDetails.some(
                                  (detail, i) => detail.name === m.name && i !== idx
                                )
                            )
                            .map((m, i) => (
                              <option key={i} value={m.name}>
                                {m.name}
                              </option>
                            ))}
                        </select>

                        {/* Amount / Percentage */}
                        {newExpense.splitType === "CUSTOM" ? (
                          <input
                            type="number"
                            placeholder="Share Amount"
                            value={sd.shareAmount}
                            onChange={(e) => {
                              const updated = [...newExpense.splitDetails];
                              updated[idx].shareAmount = parseFloat(e.target.value);
                              setNewExpense({ ...newExpense, splitDetails: updated });
                            }}
                            className="border p-2 rounded-xl w-full sm:w-24"
                          />
                        ) : (
                          <input
                            type="number"
                            placeholder="Percentage"
                            value={sd.percentage}
                            onChange={(e) => {
                              const updated = [...newExpense.splitDetails];
                              updated[idx].percentage = parseFloat(e.target.value);
                              setNewExpense({ ...newExpense, splitDetails: updated });
                            }}
                            className="border p-2 rounded-xl w-full sm:w-24"
                          />
                        )}

                        {/* Remove Person */}
                        <button
                          onClick={() => {
                            const updated = newExpense.splitDetails.filter(
                              (_, i) => i !== idx
                            );
                            setNewExpense({ ...newExpense, splitDetails: updated });
                          }}
                          className="cursor-pointer bg-red-500 text-white px-3 py-1 rounded-xl w-full sm:w-auto"
                        >
                          ✖
                        </button>
                      </div>
                    ))}

                    {/* Add Person */}
                    <button
                      onClick={() => {
                        if (newExpense.splitDetails.length < members.length) {
                          setNewExpense({
                            ...newExpense,
                            splitDetails: [
                              ...newExpense.splitDetails,
                              { name: "", shareAmount: 0, percentage: 0 },
                            ],
                          });
                        } else {
                          alert("You cannot add more people than group members!");
                        }
                      }}
                      className="cursor-pointer bg-blue-500 text-white px-3 py-2 rounded-xl mt-2 w-full sm:w-auto"
                    >
                      + Add Person
                    </button>
                  </div>
                )}

              {/* Save Button */}
              <button
                disabled={savingExpense}
                onClick={async () => {
                  setSavingExpense(true);
                  try {
                    const payload = {
                      description: newExpense.description,
                      amount: parseFloat(newExpense.amount),
                      currency: "INR",
                      groupcode: selectedGroup.code,
                      splitType: newExpense.splitType,
                      payername: newExpense.payername,
                      date: newExpense.date,
                      splitDetails:
                        newExpense.splitType === "EQUAL"
                          ? []
                          : newExpense.splitDetails,
                    };

                    await api.post("/Expense/Create", payload, {
                      headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                      },
                    });

                    toast.success("Expense added successfully!");
                    setIsAddModalOpen(false);
                    setNewExpense({
                      description: "",
                      amount: "",
                      currency: "INR",
                      splitType: "EQUAL",
                      payername: "",
                      date: new Date().toISOString().slice(0, 10),
                      splitDetails: [],
                    });

                    // 🔹 Refresh expenses
                    const res = await api.get(
                      `/Groups/Get-Expenses/${selectedGroup.code}`,
                      {
                        headers: {
                          Authorization: `Bearer ${localStorage.getItem("token")}`,
                        },
                      }
                    );
                    setExpenses(Array.isArray(res.data) ? res.data : [res.data]);

                    // 🔹 Refresh settlements
                    await fetchTransactions();
                  } catch (err) {
                    toast.error(err.response?.data || "Failed to add expense");
                  } finally {
                    setSavingExpense(false);
                  }
                }}
                className={`cursor-pointer px-3 py-2 rounded-xl mt-2 w-full sm:w-auto ${savingExpense
                    ? "bg-gray-400 text-white cursor-not-allowed"
                    : "bg-green-500 text-white hover:bg-green-600"
                  }`}
              >
                {savingExpense ? (
                  <span className="flex items-center justify-center gap-2">
                    Saving
                    <span className="animate-pulse">.</span>
                    <span className="animate-pulse delay-150">.</span>
                    <span className="animate-pulse delay-300">.</span>
                  </span>
                ) : (
                  "Save Expense"
                )}
              </button>


            </div>

          </div>
        </div>

      )}
    </div>
  );
}

