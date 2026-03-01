import { graphqlRequest } from "@/lib/graphql";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

interface ApiResponse<T> {
  statusCode: number;
  message: string;
  data: T;
}

interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

let authToken: string | null = null;

export const setAuthToken = (token: string | null) => {
  authToken = token;
  if (token) localStorage.setItem("token", token);
  else localStorage.removeItem("token");
};

export const getAuthToken = () => {
  if (!authToken && typeof window !== "undefined") {
    authToken = localStorage.getItem("token");
  }
  return authToken;
};

const fetchApi = async <T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<ApiResponse<T>> => {
  const token = getAuthToken();
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
    ...options.headers,
  };

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });
    const data = await response.json();

    if (!response.ok) {
      if (response.status === 401) {
        setAuthToken(null);
      }
      throw new Error(data.message || `API Error: ${response.statusText}`);
    }

    return data;
  } catch (error: any) {
    throw new Error(error.message || "Network error occurred");
  }
};

export const userApi = {
  register: (data: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    role?: string;
  }) =>
    fetchApi<{
      token: string;
      email: string;
      role: string;
      firstName: string;
      lastName: string;
      id: number;
    }>("/users/register", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  login: (data: { email: string; password: string }) =>
    fetchApi<{
      token: string;
      email: string;
      role: string;
      firstName: string;
      lastName: string;
      id: number;
    }>("/users/login", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  getProfile: () => fetchApi<any>("/users/profile"),
  updateProfile: (data: any) =>
    fetchApi<any>("/users/profile", {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  getAllUsers: (page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(`/users?page=${page}&size=${size}`),
  getUserById: (id: number) => fetchApi<any>(`/users/${id}`),
  updateUser: (id: number, data: any) =>
    fetchApi<any>(`/users/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  deleteUser: (id: number) =>
    fetchApi<void>(`/users/${id}`, { method: "DELETE" }),
  checkEmailExists: (email: string) =>
    fetchApi<boolean>(`/users/check-email?email=${encodeURIComponent(email)}`),
  getRoleStats: () => fetchApi<any[]>("/users/role-stats"),
};

export const categoryApi = {
  add: (data: { name: string; description?: string }) =>
    fetchApi<any>("/categories", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  getAll: (page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(`/categories?page=${page}&size=${size}`),
  getAllGraphQL: async () => {
    const query = `
      query {
        allCategories {
          id
          name
          description
        }
      }
    `;
    const data = await graphqlRequest(query);
    return { data: { content: data.allCategories } };
  },
  getById: (id: number) => fetchApi<any>(`/categories/${id}`),
  update: (id: number, data: { name?: string; description?: string }) =>
    fetchApi<any>(`/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    fetchApi<void>(`/categories/${id}`, { method: "DELETE" }),
  getByName: (name: string) =>
    fetchApi<any>(`/categories/name/${encodeURIComponent(name)}`),
  getWithProductCount: () => fetchApi<any[]>("/categories/with-count"),
};

export const productApi = {
  add: (data: {
    name: string;
    description?: string;
    price: number;
    categoryId: number;
    sku: string;
    imageUrl?: string;
  }) =>
    fetchApi<any>("/products", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  addBulk: (data: any[]) =>
    fetchApi<any[]>("/products/bulk", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  getAll: (params?: {
    page?: number;
    size?: number;
    categoryId?: number;
    search?: string;
    minPrice?: number;
    maxPrice?: number;
    inStock?: boolean;
    sortBy?: string;
    ascending?: boolean;
    algorithm?: string;
  }) => {
    const query = new URLSearchParams();
    if (params?.page !== undefined)
      query.append("page", params.page.toString());
    if (params?.size !== undefined)
      query.append("size", params.size.toString());
    if (params?.categoryId)
      query.append("categoryId", params.categoryId.toString());
    if (params?.search) query.append("search", params.search);
    if (params?.minPrice !== undefined)
      query.append("minPrice", params.minPrice.toString());
    if (params?.maxPrice !== undefined)
      query.append("maxPrice", params.maxPrice.toString());
    if (params?.inStock !== undefined)
      query.append("inStock", params.inStock.toString());
    if (params?.sortBy) query.append("sortBy", params.sortBy);
    if (params?.ascending !== undefined)
      query.append("ascending", params.ascending.toString());
    if (params?.algorithm) query.append("algorithm", params.algorithm);
    return fetchApi<PagedResponse<any>>(`/products?${query}`);
  },
  getAllGraphQL: async () => {
    const query = `
      query {
        allProducts {
          id
          name
          price
          quantity
          categoryName
          imageUrl
        }
      }
    `;
    const data = await graphqlRequest(query);
    return { data: { content: data.allProducts } };
  },
  getById: (id: number) => fetchApi<any>(`/products/${id}`),
  getByIdGraphQL: async (id: number) => {
    const query = `
      query {
        productById(id: ${id}) {
          id
          name
          price
          quantity
          categoryName
        }
      }
    `;
    const data = await graphqlRequest(query);
    return { data: data.productById };
  },
  update: (id: number, data: any) =>
    fetchApi<any>(`/products/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    fetchApi<void>(`/products/${id}`, { method: "DELETE" }),
  getByCategoryOptimized: (categoryId: number, page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(
      `/products/category/${categoryId}/optimized?page=${page}&size=${size}`,
    ),
  getStatistics: () => fetchApi<any[]>("/products/statistics"),
};

export const orderApi = {
  create: (data: { items: { productId: number; quantity: number }[] }) =>
    fetchApi<any>("/orders", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  getAll: (params?: {
    page?: number;
    size?: number;
    sortBy?: string;
    ascending?: boolean;
    algorithm?: string;
  }) => {
    const query = new URLSearchParams();
    if (params?.page !== undefined)
      query.append("page", params.page.toString());
    if (params?.size !== undefined)
      query.append("size", params.size.toString());
    if (params?.sortBy) query.append("sortBy", params.sortBy);
    if (params?.ascending !== undefined)
      query.append("ascending", params.ascending.toString());
    if (params?.algorithm) query.append("algorithm", params.algorithm);
    return fetchApi<PagedResponse<any>>(`/orders?${query}`);
  },
  getUserOrders: (page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(`/orders/user?page=${page}&size=${size}`),
  getById: (id: number) => fetchApi<any>(`/orders/${id}`),
  updateStatus: (id: number, data: { status: string }) =>
    fetchApi<any>(`/orders/${id}/status`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  delete: (id: number) => fetchApi<void>(`/orders/${id}`, { method: "DELETE" }),
  getOrderItems: (orderId: number) =>
    fetchApi<any[]>(`/orders/${orderId}/items`),
  getHighValueOrders: (minAmount: number, page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(
      `/orders/high-value?minAmount=${minAmount}&page=${page}&size=${size}`,
    ),
  getRevenueReport: (startDate: string, endDate: string) =>
    fetchApi<any[]>(
      `/orders/revenue?startDate=${startDate}&endDate=${endDate}`,
    ),
  getBestSellers: (limit = 10) =>
    fetchApi<any[]>(`/orders/best-sellers?limit=${limit}`),
};

export const inventoryApi = {
  add: (data: { productId: number; quantity: number; location: string }) =>
    fetchApi<any>("/inventory", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  getAll: (page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(`/inventory?page=${page}&size=${size}`),
  getById: (id: number) => fetchApi<any>(`/inventory/${id}`),
  getByProductId: (productId: number) =>
    fetchApi<any>(`/inventory/product/${productId}`),
  update: (id: number, data: { quantity?: number; location?: string }) =>
    fetchApi<any>(`/inventory/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  adjustQuantity: (id: number, quantity: number) =>
    fetchApi<any>(`/inventory/${id}?quantity=${quantity}`, {
      method: "PATCH",
    }),
  delete: (id: number) =>
    fetchApi<void>(`/inventory/${id}`, { method: "DELETE" }),
  updateStockByProductId: (productId: number, quantity: number) =>
    fetchApi<void>(
      `/inventory/product/${productId}/stock?quantity=${quantity}`,
      {
        method: "PUT",
      },
    ),
  decrementStock: (productId: number, quantity: number) =>
    fetchApi<void>(
      `/inventory/product/${productId}/decrement?quantity=${quantity}`,
      {
        method: "POST",
      },
    ),
  incrementStock: (productId: number, quantity: number) =>
    fetchApi<void>(
      `/inventory/product/${productId}/increment?quantity=${quantity}`,
      {
        method: "POST",
      },
    ),
  getLowStock: (threshold = 10) =>
    fetchApi<any[]>(`/inventory/low-stock?threshold=${threshold}`),
  deleteByProductId: (productId: number) =>
    fetchApi<void>(`/inventory/product/${productId}`, { method: "DELETE" }),
};

export const reviewApi = {
  add: (data: { productId: number; rating: number; comment?: string }) =>
    fetchApi<any>("/reviews", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  getAll: (page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(`/reviews?page=${page}&size=${size}`),
  getById: (id: number) => fetchApi<any>(`/reviews/${id}`),
  getByProductId: (productId: number, page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(
      `/reviews/product/${productId}?page=${page}&size=${size}`,
    ),
  getUserReviews: (page = 0, size = 10) =>
    fetchApi<PagedResponse<any>>(`/reviews/user?page=${page}&size=${size}`),
  update: (id: number, data: { rating?: number; comment?: string }) =>
    fetchApi<any>(`/reviews/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    fetchApi<void>(`/reviews/${id}`, { method: "DELETE" }),
  getByUserAndProduct: (userId: number, productId: number) =>
    fetchApi<any>(`/reviews/user/${userId}/product/${productId}`),
  hasUserReviewed: (userId: number, productId: number) =>
    fetchApi<boolean>(`/reviews/check?userId=${userId}&productId=${productId}`),
  getRatingDistribution: (productId: number) =>
    fetchApi<any[]>(`/reviews/product/${productId}/rating`),
  getReviewSummary: (productId: number) =>
    fetchApi<any>(`/reviews/product/${productId}/summary`),
};

export const cartApi = {
  get: () => fetchApi<any>("/cart"),
  addItem: (data: { productId: number; quantity: number }) =>
    fetchApi<any>("/cart", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateItem: (itemId: number, data: { quantity: number }) =>
    fetchApi<any>(`/cart/${itemId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  removeItem: (itemId: number) =>
    fetchApi<any>(`/cart/${itemId}`, { method: "DELETE" }),
  clear: () => fetchApi<void>("/cart/clear", { method: "DELETE" }),
  checkout: () => fetchApi<any>("/cart/checkout", { method: "POST" }),
  getSummary: () => fetchApi<any>("/cart/summary"),
  updateItemByProduct: (productId: number, quantity: number) =>
    fetchApi<any>(`/cart/product/${productId}?quantity=${quantity}`, {
      method: "PATCH",
    }),
  exists: () => fetchApi<boolean>("/cart/exists"),
  deleteCart: () => fetchApi<void>("/cart/user", { method: "DELETE" }),
};

export const authApi = {
  logout: () => fetchApi<void>("/auth/logout", { method: "POST" }),
  logoutAll: () => fetchApi<void>("/auth/logout-all", { method: "POST" }),
  validateSession: () => fetchApi<boolean>("/auth/validate"),
  cleanExpiredSessions: () =>
    fetchApi<number>("/auth/sessions/expired", { method: "DELETE" }),
  getActiveSessionCount: () => fetchApi<number>("/auth/sessions/active-count"),
};
