'use client';
import { useState, useEffect } from 'react';
import { categoryApi } from '@/lib/api';
import { useRouter, useParams } from 'next/navigation';

export default function EditCategory() {
  const router = useRouter();
  const params = useParams();
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({ name: '', description: '' });

  useEffect(() => {
    categoryApi.getById(Number(params.id)).then(res => {
      const c = res.data;
      setFormData({ name: c.name, description: c.description || '' });
    });
  }, [params.id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await categoryApi.update(Number(params.id), formData);
      router.push('/admin');
    } catch (err: any) {
      setError(err.message || 'Failed to update category');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-6">Edit Category</h1>
        {error && (
          <div className="mb-4 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block mb-2">Name</label>
            <input type="text" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} className="w-full border rounded px-3 py-2" required />
          </div>
          <div className="mb-6">
            <label className="block mb-2">Description</label>
            <textarea value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} className="w-full border rounded px-3 py-2" rows={3} />
          </div>
          <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 mr-2">Update Category</button>
          <button type="button" onClick={() => router.push('/admin')} className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700">Cancel</button>
        </form>
      </div>
    </div>
  );
}
