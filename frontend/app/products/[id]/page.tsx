"use client";
import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { productApi, reviewApi, inventoryApi, cartApi } from "@/lib/api";
import { useAuth } from "@/lib/auth-context";

export default function ProductDetail() {
  const params = useParams();
  const router = useRouter();
  const { user } = useAuth();
  const [product, setProduct] = useState<any>(null);
  const [inventory, setInventory] = useState<any>(null);
  const [reviews, setReviews] = useState<any[]>([]);
  const [quantity, setQuantity] = useState(1);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const id = Number(params.id);
    // Use GraphQL for simple product detail fetch
    productApi
      .getByIdGraphQL(id)
      .then((res) => setProduct(res.data))
      .catch((err) => setError(err.message));

    inventoryApi
      .getByProductId(id)
      .then((res) => setInventory(res.data))
      .catch(() => {});

    reviewApi
      .getByProductId(id)
      .then((res) => setReviews(res.data.content))
      .catch(() => {});
  }, [params.id]);

  const handleAddToCart = async () => {
    if (!user) {
      router.push("/login");
      return;
    }
    try {
      if (quantity < 1) {
        setError("Quantity must be at least 1");
        setMessage("");
        return;
      }

      await cartApi.addItem({ productId: product.id, quantity });
      setMessage("Item added to cart successfully!");
      setError("");
      setTimeout(() => setMessage(""), 3000);
    } catch (err: any) {
      setError(err.message || "Failed to add to cart");
      setMessage("");
    }
  };

  const handleAddReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      router.push("/login");
      return;
    }
    try {
      await reviewApi.add({ productId: product.id, rating, comment });
      setMessage("Review added successfully!");
      setError("");
      setRating(5);
      setComment("");
      // Refresh reviews
      const res = await reviewApi.getByProductId(product.id);
      setReviews(res.data.content);
    } catch (err: any) {
      setError(err.message || "Failed to add review");
      setMessage("");
    }
  };

  if (!product)
    return (
      <div className="min-h-screen flex items-center justify-center">
        Loading...
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-6xl mx-auto px-4">
        <button
          onClick={() => router.push("/")}
          className="mb-4 text-blue-600 hover:underline"
        >
          ← Back to Products
        </button>

        <div className="bg-white rounded-lg shadow p-8 mb-8">
          {
            <img
              src={product.imageUrl || "https://placehold.net/1.png"}
              alt={product.name}
              className="w-full h-96 object-cover rounded mb-6"
            />
          }
          <h1 className="text-3xl font-bold mb-4">{product.name}</h1>
          <p className="text-gray-600 mb-4">{product.description}</p>
          <p className="text-3xl font-bold text-green-600 mb-4">
            ${product.price}
          </p>
          {inventory && (
            <p className="text-sm text-gray-600 mb-4">
              Stock: {inventory.quantity} available
            </p>
          )}

          {message && <p className="mb-4 text-green-600">{message}</p>}
          {error && <p className="mb-4 text-red-600">{error}</p>}

          <div className="flex gap-4 items-center">
            <input
              type="number"
              min="1"
              max={inventory?.quantity || 1}
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
              className="border rounded px-3 py-2 w-20"
            />
            <button
              onClick={handleAddToCart}
              className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
            >
              Add to Cart
            </button>
            <button
              onClick={() => router.push("/cart")}
              className="bg-gray-600 text-white px-6 py-2 rounded hover:bg-gray-700"
            >
              View Cart
            </button>
          </div>
        </div>

        {/* Reviews Section */}
        <div className="bg-white rounded-lg shadow p-8 mb-8">
          <h2 className="text-2xl font-bold mb-4">Customer Reviews</h2>

          {user && user.role === "CUSTOMER" && (
            <form onSubmit={handleAddReview} className="mb-6 border-b pb-6">
              <h3 className="text-lg font-semibold mb-2">Write a Review</h3>
              <div className="mb-4">
                <label className="block mb-2">Rating:</label>
                <select
                  value={rating}
                  onChange={(e) => setRating(Number(e.target.value))}
                  className="border rounded px-3 py-2"
                >
                  <option value={5}>5 Stars</option>
                  <option value={4}>4 Stars</option>
                  <option value={3}>3 Stars</option>
                  <option value={2}>2 Stars</option>
                  <option value={1}>1 Star</option>
                </select>
              </div>
              <div className="mb-4">
                <label className="block mb-2">Comment:</label>
                <textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  className="border rounded px-3 py-2 w-full"
                  rows={4}
                  placeholder="Share your thoughts..."
                />
              </div>
              <button
                type="submit"
                className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
              >
                Submit Review
              </button>
            </form>
          )}

          <div className="space-y-4">
            {reviews.length === 0 ? (
              <p className="text-gray-500">
                No reviews yet. Be the first to review this product!
              </p>
            ) : (
              reviews.map((review) => (
                <div key={review.id} className="border-b pb-4">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <span className="font-semibold">{review.userName}</span>
                      <span className="text-yellow-500 ml-2">
                        {"★".repeat(review.rating)}
                        {"☆".repeat(5 - review.rating)}
                      </span>
                    </div>
                    <span className="text-sm text-gray-500">
                      {new Date(review.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  {review.comment && (
                    <p className="text-gray-700">{review.comment}</p>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
