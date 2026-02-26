"use client";
import { useState, useEffect } from "react";
import { useAuth } from "@/lib/auth-context";
import { orderApi } from "@/lib/api";
import { useRouter } from "next/navigation";

export default function Orders() {
  const { user } = useAuth();
  const router = useRouter();
  const [orders, setOrders] = useState<any[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    if (!user) router.push("/login");
    else {
      orderApi.getUserOrders(page, 10).then((res) => {
        setOrders(res.data.content);
        setTotalPages(res.data.totalPages);
      });
    }
  }, [user, page, router]);

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-6xl mx-auto px-4">
        <h1 className="text-3xl font-bold mb-6">My Orders</h1>
        <button
          onClick={() => router.push("/")}
          className="mb-4 bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
        >
          Back to Home
        </button>
        {orders.length === 0 ? (
          <p>No orders found</p>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div key={order.id} className="bg-white rounded-lg shadow p-6">
                <div className="flex justify-between mb-4">
                  <div>
                    <p className="font-bold">Order #{order.id}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-xl">${order.totalAmount}</p>
                    <p
                      className={`text-sm ${order.status === "DELIVERED" ? "text-green-600" : "text-yellow-600"}`}
                    >
                      {order.status}
                    </p>
                  </div>
                </div>
                <div className="border-t pt-4">
                  <h3 className="font-semibold mb-2">Items:</h3>
                  {order.items?.map((item: any, idx: number) => (
                    <div
                      key={idx}
                      className="flex justify-between text-sm mb-1"
                    >
                      <span>
                        {item.productName} x {item.quantity}
                      </span>
                      <span>${item.price}</span>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
        {totalPages > 1 && (
          <div className="flex justify-center gap-2 mt-8">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
            >
              Previous
            </button>
            <span className="px-4 py-2">
              Page {page + 1} of {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => p + 1)}
              disabled={page >= totalPages - 1}
              className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
