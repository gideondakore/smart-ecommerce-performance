"use client";
import { useState, useEffect } from "react";
import { useAuth } from "@/lib/auth-context";
import {
  productApi,
  categoryApi,
  orderApi,
  inventoryApi,
  userApi,
} from "@/lib/api";
import { useRouter } from "next/navigation";

export default function Admin() {
  const { user } = useAuth();
  const router = useRouter();
  const [tab, setTab] = useState("products");
  const [products, setProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [orders, setOrders] = useState<any[]>([]);
  const [users, setUsers] = useState<any[]>([]);
  const [inventories, setInventories] = useState<any[]>([]);

  useEffect(() => {
    if (!user || user.role !== "ADMIN") router.push("/");
  }, [user, router]);

  useEffect(() => {
    if (tab === "products")
      productApi
        .getAll({ page: 0, size: 50 })
        .then((res) => setProducts(res.data.content));
    if (tab === "categories")
      categoryApi.getAll(0, 50).then((res) => setCategories(res.data.content));
    if (tab === "orders")
      orderApi
        .getAll({ page: 0, size: 50 })
        .then((res) => setOrders(res.data.content));
    if (tab === "users")
      userApi.getAllUsers(0, 50).then((res) => setUsers(res.data.content));
    if (tab === "inventory")
      inventoryApi
        .getAll(0, 50)
        .then((res) => setInventories(res.data.content));
  }, [tab]);

  const deleteProduct = async (id: number) => {
    if (confirm("Delete this product?")) {
      await productApi.delete(id);
      setProducts(products.filter((p) => p.id !== id));
    }
  };

  const deleteCategory = async (id: number) => {
    if (confirm("Delete this category?")) {
      await categoryApi.delete(id);
      setCategories(categories.filter((c) => c.id !== id));
    }
  };

  const deleteUser = async (id: number) => {
    if (confirm("Delete this user?")) {
      await userApi.deleteUser(id);
      setUsers(users.filter((u) => u.id !== id));
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4">
        <h1 className="text-3xl font-bold mb-6">Admin Dashboard</h1>
        <button
          onClick={() => router.push("/")}
          className="mb-4 bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
        >
          Back to Home
        </button>

        <div className="flex gap-2 mb-6">
          {["products", "categories", "orders", "users", "inventory"].map(
            (t) => (
              <button
                key={t}
                onClick={() => setTab(t)}
                className={`px-4 py-2 rounded ${tab === t ? "bg-blue-600 text-white" : "bg-white"}`}
              >
                {t.charAt(0).toUpperCase() + t.slice(1)}
              </button>
            ),
          )}
        </div>

        {tab === "products" && (
          <div>
            <button
              onClick={() => router.push("/admin/products/add")}
              className="mb-4 bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              Add Product
            </button>
            <div className="bg-white rounded-lg shadow overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="px-4 py-2 text-left">ID</th>
                    <th className="px-4 py-2 text-left">Name</th>
                    <th className="px-4 py-2 text-left">Price</th>
                    <th className="px-4 py-2 text-left">Category</th>
                    <th className="px-4 py-2 text-left">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {products.map((p) => (
                    <tr key={p.id} className="border-t">
                      <td className="px-4 py-2">{p.id}</td>
                      <td className="px-4 py-2">{p.name}</td>
                      <td className="px-4 py-2">${p.price}</td>
                      <td className="px-4 py-2">{p.categoryName}</td>
                      <td className="px-4 py-2">
                        <button
                          onClick={() =>
                            router.push(`/admin/products/edit/${p.id}`)
                          }
                          className="text-blue-600 hover:underline mr-2"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => deleteProduct(p.id)}
                          className="text-red-600 hover:underline"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {tab === "categories" && (
          <div>
            <button
              onClick={() => router.push("/admin/categories/add")}
              className="mb-4 bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              Add Category
            </button>
            <div className="bg-white rounded-lg shadow overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="px-4 py-2 text-left">ID</th>
                    <th className="px-4 py-2 text-left">Name</th>
                    <th className="px-4 py-2 text-left">Description</th>
                    <th className="px-4 py-2 text-left">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {categories.map((c) => (
                    <tr key={c.id} className="border-t">
                      <td className="px-4 py-2">{c.id}</td>
                      <td className="px-4 py-2">{c.name}</td>
                      <td className="px-4 py-2">{c.description}</td>
                      <td className="px-4 py-2">
                        <button
                          onClick={() =>
                            router.push(`/admin/categories/edit/${c.id}`)
                          }
                          className="text-blue-600 hover:underline mr-2"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => deleteCategory(c.id)}
                          className="text-red-600 hover:underline"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {tab === "orders" && (
          <div className="bg-white rounded-lg shadow overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-4 py-2 text-left">ID</th>
                  <th className="px-4 py-2 text-left">User</th>
                  <th className="px-4 py-2 text-left">Total</th>
                  <th className="px-4 py-2 text-left">Status</th>
                  {/* <th className="px-4 py-2 text-left">Date</th> */}
                  <th className="px-4 py-2 text-left">Actions</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.id} className="border-t">
                    <td className="px-4 py-2">{o.id}</td>
                    <td className="px-4 py-2">{o.userName}</td>
                    <td className="px-4 py-2">${o.totalAmount}</td>
                    <td className="px-4 py-2">{o.status}</td>
                    {/* <td className="px-4 py-2">{new Date(o.orderDate).toLocaleDateString()}</td> */}
                    <td className="px-4 py-2">
                      <button
                        onClick={() => router.push(`/admin/orders/${o.id}`)}
                        className="text-blue-600 hover:underline"
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {tab === "users" && (
          <div className="bg-white rounded-lg shadow overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-4 py-2 text-left">ID</th>
                  <th className="px-4 py-2 text-left">Username</th>
                  <th className="px-4 py-2 text-left">Email</th>
                  <th className="px-4 py-2 text-left">Role</th>
                  <th className="px-4 py-2 text-left">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id} className="border-t">
                    <td className="px-4 py-2">{u.id}</td>
                    <td className="px-4 py-2">{u.username}</td>
                    <td className="px-4 py-2">{u.email}</td>
                    <td className="px-4 py-2">{u.role}</td>
                    <td className="px-4 py-2">
                      <button
                        onClick={() => deleteUser(u.id)}
                        className="text-red-600 hover:underline"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {tab === "inventory" && (
          <div>
            <button
              onClick={() => router.push("/admin/inventory/add")}
              className="mb-4 bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              Add Inventory
            </button>
            <div className="bg-white rounded-lg shadow overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="px-4 py-2 text-left">ID</th>
                    <th className="px-4 py-2 text-left">Product</th>
                    <th className="px-4 py-2 text-left">Quantity</th>
                    <th className="px-4 py-2 text-left">Reorder Level</th>
                    <th className="px-4 py-2 text-left">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {inventories.map((i) => (
                    <tr key={i.id} className="border-t">
                      <td className="px-4 py-2">{i.id}</td>
                      <td className="px-4 py-2">{i.productName}</td>
                      <td className="px-4 py-2">{i.quantity}</td>
                      <td className="px-4 py-2">{i.reorderLevel}</td>
                      <td className="px-4 py-2">
                        <button
                          onClick={() =>
                            router.push(`/admin/inventory/edit/${i.id}`)
                          }
                          className="text-blue-600 hover:underline"
                        >
                          Edit
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
