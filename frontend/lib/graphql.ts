const API_BASE =
  process.env.NEXT_PUBLIC_API_URL?.replace("/api", "") ??
  "http://localhost:8080";
const GRAPHQL_URL = `${API_BASE}/graphql`;

export const graphqlRequest = async (query: string, variables?: any) => {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("token") : null;

  const response = await fetch(GRAPHQL_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: JSON.stringify({ query, variables }),
  });

  const result = await response.json();

  if (result.errors) {
    throw new Error(result.errors[0].message);
  }

  return result.data;
};
