"use client";
import { useState, useEffect } from "react";
import { inventoryApi } from "@/lib/api";
import { useRouter, useParams } from "next/navigation";

export default function EditInventory() {
  const router = useRouter();
  const params = useParams();
  const [error, setError] = useState("");
  const [formData, setFormData] = useState({
    quantity: 0,
    reorderLevel: 0,
    location: "",
  });
  const [productName, setProductName] = useState("");

  useEffect(() => {
    inventoryApi.getById(Number(params.id)).then((res) => {
      const i = res.data;
      setFormData({
        quantity: i.quantity || 0,
        reorderLevel: i.reorderLevel || 0,
        location: i.location || "",
      });
      setProductName(i.productName);
    });
  }, [params.id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await inventoryApi.update(Number(params.id), formData);
      router.push("/admin");
    } catch (err: any) {
      setError(err.message || "Failed to update inventory");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-6">Edit Inventory</h1>
        <p className="mb-4 text-gray-600">Product: {productName}</p>
        {error && (
          <div className="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block mb-2">Quantity</label>
            <input
              type="number"
              value={formData.quantity}
              onChange={(e) =>
                setFormData({ ...formData, quantity: Number(e.target.value) })
              }
              className="w-full border rounded px-3 py-2"
              required
            />
          </div>
          {/* <div className="mb-6">
            <label className="block mb-2">Reorder Level</label>
            <input type="number" value={formData.reorderLevel} onChange={(e) => setFormData({ ...formData, reorderLevel: Number(e.target.value) })} className="w-full border rounded px-3 py-2" />
          </div> */}
          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 mr-2"
          >
            Update Inventory
          </button>
          <button
            type="button"
            onClick={() => router.push("/admin")}
            className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
          >
            Cancel
          </button>
        </form>
      </div>
    </div>
  );
}
