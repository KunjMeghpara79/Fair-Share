import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import api from "../api/axios"; // adjust path


export default function GroupDetails({ selectedGroup, onBack }) {
    const [groupData, setGroupData] = useState(null);

    

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
            {/* Back Button */}
            <button
                onClick={onBack}
                className="cursor-pointer self-start px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
            >
                ← Back to Groups
            </button>

            {/* Group Header */}
            <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4">
                <h1 className="text-3xl font-bold">{groupData?.name || selectedGroup.name || "Group Name"}</h1>
                <p className="text-gray-600 text-sm sm:text-base">
                    <b>Code: </b><span className="font-mono">{groupData?.code || selectedGroup.code}</span>
                </p>
            </div>

            {/* Group Description */}
            <p className="text-gray-600"><b>Group Description : </b>{selectedGroup.description || "No description available."}</p>

            {/* Expenses Section */}
            <div className="bg-white rounded-xl shadow p-6 flex flex-col gap-4">
                <h2 className="text-xl font-semibold">Expenses</h2>
                {/* {groupData?.expenses?.length > 0 ? (
                    groupData.expenses.map((expense) => (
                        <div key={expense.id} className="text-gray-700">
                            {expense.name} - ${expense.amount}
                        </div>
                    ))
                ) : (
                    <div className="text-gray-500">No expenses yet.</div>
                )} */}
            </div>
        </div>
    );
}
