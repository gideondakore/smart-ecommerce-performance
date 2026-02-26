"use client";
import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth-context";
import { productApi, categoryApi } from "@/lib/api";
import Link from "next/link";

export default function Home() {
  const { user, logout } = useAuth();
  const [products, setProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<
    number | undefined
  >();
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    // Use GraphQL for simple category list
    categoryApi.getAllGraphQL().then((res) => setCategories(res.data.content));
  }, []);

  useEffect(() => {
    // Use GraphQL for simple listing, REST for filtering/search
    const hasFilters = selectedCategory || searchQuery || page > 0;

    if (hasFilters) {
      // Use REST API for complex queries with filters
      productApi
        .getAll({
          page,
          size: 12,
          categoryId: selectedCategory,
          search: searchQuery || undefined,
        })
        .then((res) => {
          console.log("REST products response:", res);
          setProducts(res.data.content);
          setTotalPages(res.data.totalPages);
        });
    } else {
      // Use GraphQL for simple initial load
      productApi.getAllGraphQL().then((res) => {
        console.log("GraphQL products response:", res);
        setProducts(res.data.content.slice(0, 12));
        setTotalPages(Math.ceil(res.data.content.length / 12));
      });
    }

    console.log("Fetching products with filters:", products);
  }, [page, selectedCategory, searchQuery]);

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <img
              src="/smart_shop.jpeg"
              alt="Smart Shop"
              className="h-10 w-10 object-contain"
            />
            <h1 className="text-2xl font-bold">Smart Shop</h1>
          </div>
          <div className="flex gap-4">
            {user ? (
              <>
                {user.role === "ADMIN" && (
                  <>
                    <Link
                      href="/dashboard/admin"
                      className="text-blue-600 hover:underline"
                    >
                      Dashboard
                    </Link>
                    <Link
                      href="/admin"
                      className="text-blue-600 hover:underline"
                    >
                      Manage Store
                    </Link>
                  </>
                )}
                {user.role === "VENDOR" && (
                  <>
                    <Link
                      href="/dashboard/vendor"
                      className="text-blue-600 hover:underline"
                    >
                      Dashboard
                    </Link>
                  </>
                )}
                {user.role === "CUSTOMER" && (
                  <>
                    <Link
                      href="/cart"
                      className="text-blue-600 hover:underline"
                    >
                      Cart
                    </Link>
                    <Link
                      href="/dashboard/customer"
                      className="text-blue-600 hover:underline"
                    >
                      Dashboard
                    </Link>
                  </>
                )}
                <Link href="/profile" className="text-blue-600 hover:underline">
                  Profile
                </Link>
                <button
                  onClick={logout}
                  className="text-red-600 hover:underline"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link href="/login" className="text-blue-600 hover:underline">
                  Login
                </Link>
                <Link
                  href="/register"
                  className="text-blue-600 hover:underline"
                >
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-6">
          <input
            type="text"
            placeholder="Search products..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full md:w-1/2 border rounded px-4 py-2 mb-4"
          />
          <label className="block mb-2 font-semibold">
            Filter by Category:
          </label>
          <select
            value={selectedCategory || ""}
            onChange={(e) => {
              setSelectedCategory(
                e.target.value ? Number(e.target.value) : undefined,
              );
              setPage(0);
            }}
            className="border rounded px-4 py-2"
          >
            <option value="">All Categories</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </select>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {products.map((product) => (
            <div
              key={product.id}
              className="bg-white rounded-lg shadow p-4 relative"
            >
              {product.quantity === 0 && (
                <div className="absolute top-2 right-2 bg-red-600 text-white px-2 py-1 rounded text-sm font-bold">
                  OUT OF STOCK
                </div>
              )}
              {
                <img
                  src={product.imageUrl || "https://placehold.net/1.png"}
                  alt={product.name}
                  className="w-full h-48 object-cover rounded mb-4"
                />
              }
              <h3 className="font-bold text-lg mb-2">{product.name}</h3>
              <p className="text-gray-600 text-sm mb-2">
                {product.description}
              </p>
              <p className="text-xl font-bold text-green-600">
                ${product.price}
              </p>
              {product.quantity > 0 && product.quantity < 10 && (
                <p className="text-orange-600 text-sm mt-2">
                  Only {product.quantity} left!
                </p>
              )}
              <Link
                href={`/products/${product.id}`}
                className="mt-4 block text-center bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
              >
                View Details
              </Link>
            </div>
          ))}
        </div>

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
