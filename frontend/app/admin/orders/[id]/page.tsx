"use client";
import { useState, useEffect, use } from "react";
import { orderApi } from "@/lib/api";
import { useRouter, useParams } from "next/navigation";
import { useAuth } from "@/lib/auth-context";

export default function OrderDetail() {
  const router = useRouter();
  const params = useParams();
  const { user } = useAuth();
  const [order, setOrder] = useState<any>(null);
  const [status, setStatus] = useState("");
  const [error, setError] = useState('');

  useEffect(() => {
    orderApi.getById(Number(params.id)).then((res) => {
      setOrder(res.data);
      setStatus(res.data.status);
    });
  }, [params.id]);

  const handleUpdateStatus = async () => {
    setError('');
    try {
      await orderApi.updateStatus(Number(params.id), { status });
      const res = await orderApi.getById(Number(params.id));
      setOrder(res.data);
    } catch (err: any) {
      setError(err.message || 'Failed to update order status');
    }
  };

  if (!order)
    return (
      <div className="min-h-screen flex items-center justify-center">
        Loading...
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-6">Order #{order.id}</h1>
        {error && (
          <div className="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}
        <div className="mb-6">
          <p className="mb-2">
            <strong>User:</strong> {order.userName}
          </p>
          <p className="mb-2">
            <strong>Date:</strong> {new Date(order.createdAt).toLocaleString()}
          </p>
          <p className="mb-2">
            <strong>Total:</strong> ${order.totalAmount}
          </p>
          <p className="mb-4">
            <strong>Status:</strong> {order.status}
          </p>
        </div>
        <div className="mb-6">
          <h2 className="text-xl font-bold mb-4">Items</h2>
          <table className="w-full border">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-4 py-2 text-left">Product</th>
                <th className="px-4 py-2 text-left">Quantity</th>
                <th className="px-4 py-2 text-left">Price</th>
                <th className="px-4 py-2 text-left">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {order.items?.map((item: any, idx: number) => {
                const unitPrice =
                  Number(item.totalPrice || 0) / Number(item.quantity || 1);
                return (
                  <tr key={idx} className="border-t">
                    <td className="px-4 py-2">{item.productName}</td>
                    <td className="px-4 py-2">{item.quantity}</td>
                    <td className="px-4 py-2">${unitPrice.toFixed(2)}</td>
                    <td className="px-4 py-2">
                      ${Number(item.totalPrice || 0).toFixed(2)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        {user?.role !== "CUSTOMER" && (
          <div className="mb-6">
            <label className="block mb-2 font-bold">Update Status</label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className="border rounded px-3 py-2 mr-2"
            >
              <option value="PENDING">PENDING</option>
              <option value="PROCESSING">PROCESSING</option>
              <option value="SHIPPED">SHIPPED</option>
              <option value="DELIVERED">DELIVERED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
            <button
              onClick={handleUpdateStatus}
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            >
              Update
            </button>
          </div>
        )}
        <button
          onClick={() => router.push("/admin")}
          className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
        >
          Back to Home
        </button>
      </div>
    </div>
  );
}
