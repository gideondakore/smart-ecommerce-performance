"use client";
import { useState, useEffect } from "react";
import { inventoryApi, productApi } from "@/lib/api";
import { useRouter } from "next/navigation";

export default function AddInventory() {
  const router = useRouter();
  const [products, setProducts] = useState<any[]>([]);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState({
    productId: 0,
    quantity: 0,
    location: "",
    reorderLevel: 10,
  });

  useEffect(() => {
    productApi
      .getAll({ page: 0, size: 100 })
      .then((res) => setProducts(res.data.content));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await inventoryApi.add(formData);
      router.push("/admin");
    } catch (err: any) {
      setError(err.message || "Failed to add inventory");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-6">Add Inventory</h1>
        {error && (
          <div className="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block mb-2">Product</label>
            <select
              value={formData.productId}
              onChange={(e) =>
                setFormData({ ...formData, productId: Number(e.target.value) })
              }
              className="w-full border rounded px-3 py-2"
              required
            >
              <option value="">Select Product</option>
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </div>
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
          <div className="mb-4">
            <label className="block mb-2">Location</label>
            <input
              type="text"
              value={formData.location}
              onChange={(e) =>
                setFormData({ ...formData, location: e.target.value })
              }
              className="w-full border rounded px-3 py-2"
              required
              placeholder="e.g., Warehouse A, Shelf 5"
            />
          </div>
          {/* <div className="mb-6">
            <label className="block mb-2">Reorder Level</label>
            <input
              type="number"
              value={formData.reorderLevel}
              onChange={(e) =>
                setFormData({
                  ...formData,
                  reorderLevel: Number(e.target.value),
                })
              }
              className="w-full border rounded px-3 py-2"
            />
          </div> */}
          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 mr-2"
          >
            Add Inventory
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
