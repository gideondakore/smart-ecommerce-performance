"use client";
import { useState, useEffect } from "react";
import { productApi, categoryApi } from "@/lib/api";
import { useRouter, useParams } from "next/navigation";

export default function EditProduct() {
  const router = useRouter();
  const params = useParams();
  const [categories, setCategories] = useState<any[]>([]);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState({
    name: "",
    sku: "",
    description: "",
    price: 0,
    categoryId: 0,
    imageUrl: "",
  });

  useEffect(() => {
    categoryApi.getAll(0, 100).then((res) => setCategories(res.data.content));
    productApi.getById(Number(params.id)).then((res) => {
      const p = res.data;
      setFormData({
        name: p.name,
        sku: p.sku || "",
        description: p.description,
        price: p.price,
        categoryId: p.categoryId,
        imageUrl: p.imageUrl || "",
      });
    });
  }, [params.id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await productApi.update(Number(params.id), formData);
      router.push("/admin");
    } catch (err: any) {
      setError(err.message || "Failed to update product");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-6">Edit Product</h1>
        {error && (
          <div className="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block mb-2">Name</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) =>
                setFormData({ ...formData, name: e.target.value })
              }
              className="w-full border rounded px-3 py-2"
              required
            />
          </div>
          <div className="mb-4">
            <label className="block mb-2">SKU (Stock Keeping Unit)</label>
            <input
              type="text"
              value={formData.sku}
              onChange={(e) =>
                setFormData({ ...formData, sku: e.target.value })
              }
              className="w-full border rounded px-3 py-2"
              required
              placeholder="e.g., PROD-001"
            />
          </div>
          <div className="mb-4">
            <label className="block mb-2">Description</label>
            <textarea
              value={formData.description || ""}
              onChange={(e) =>
                setFormData({ ...formData, description: e.target.value })
              }
              className="w-full border rounded px-3 py-2"
              rows={3}
            />
          </div>
          <div className="mb-4">
            <label className="block mb-2">Price</label>
            <input
              type="number"
              step="0.01"
              value={formData.price}
              onChange={(e) =>
                setFormData({ ...formData, price: Number(e.target.value) })
              }
              className="w-full border rounded px-3 py-2"
              required
            />
          </div>
          <div className="mb-4">
            <label className="block mb-2">Category</label>
            <select
              value={formData.categoryId}
              onChange={(e) =>
                setFormData({ ...formData, categoryId: Number(e.target.value) })
              }
              className="w-full border rounded px-3 py-2"
              required
            >
              <option value="">Select Category</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
          <div className="mb-6">
            <label className="block mb-2">Image URL</label>
            <input
              type="text"
              value={formData.imageUrl}
              onChange={(e) =>
                setFormData({ ...formData, imageUrl: e.target.value })
              }
              className="w-full border rounded px-3 py-2"
            />
          </div>
          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 mr-2"
          >
            Update Product
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
