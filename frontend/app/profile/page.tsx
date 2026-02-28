"use client";
import { useState, useEffect } from "react";
import { useAuth } from "@/lib/auth-context";
import { userApi } from "@/lib/api";
import { useRouter } from "next/navigation";

export default function Profile() {
  const { user } = useAuth();
  const router = useRouter();
  const [profile, setProfile] = useState<any>(null);
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState({ username: "", email: "" });

  useEffect(() => {
    if (!user) router.push("/login");
    else {
      userApi.getProfile().then((res) => {
        setProfile(res.data);
        setFormData({
          username: res.data.username ?? "",
          email: res.data.email ?? "",
        });
      });
    }
  }, [user, router]);

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await userApi.updateProfile(formData);
      const res = await userApi.getProfile();
      setProfile(res.data);
      setFormData({
        username: res.data.username ?? "",
        email: res.data.email ?? "",
      });
      setEditing(false);
    } catch (err: any) {
      setError(err.message || "Failed to update profile");
    }
  };

  if (!profile)
    return (
      <div className="min-h-screen flex items-center justify-center">
        Loading...
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-6">Profile</h1>
        {error && (
          <div className="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}
        {!editing ? (
          <div>
            <p className="mb-2">
              <strong>Username:</strong> {profile.username}
            </p>
            <p className="mb-2">
              <strong>Email:</strong> {profile.email}
            </p>
            <p className="mb-6">
              <strong>Role:</strong> {profile.role}
            </p>
            <button
              onClick={() => setEditing(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 mr-2"
            >
              Edit Profile
            </button>
            <button
              onClick={() => router.push("/")}
              className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
            >
              Back to Home
            </button>
          </div>
        ) : (
          <form onSubmit={handleUpdate}>
            <div className="mb-6">
              <label className="block mb-2">Email</label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) =>
                  setFormData({ ...formData, email: e.target.value })
                }
                className="w-full border rounded px-3 py-2"
              />
            </div>
            <button
              type="submit"
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 mr-2"
            >
              Save
            </button>
            <button
              type="button"
              onClick={() => setEditing(false)}
              className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
            >
              Cancel
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
