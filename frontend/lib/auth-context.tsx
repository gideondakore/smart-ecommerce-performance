"use client";
import {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
} from "react";
import { userApi, setAuthToken, getAuthToken } from "@/lib/api";

interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<User>;
  register: (
    firstName: string,
    lastName: string,
    email: string,
    password: string,
  ) => Promise<User>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const token = getAuthToken();
    if (token) {
      userApi
        .getProfile()
        .then((res) => {
          const profileData = res.data;
          setUser({
            id: profileData.id,
            firstName: profileData.firstName,
            lastName: profileData.lastName,
            email: profileData.email,
            role:
              typeof profileData.role === "string"
                ? profileData.role
                : profileData.role?.toString() || "CUSTOMER",
          });
        })
        .catch((error: Error) => {
          // Only clear the token on genuine auth failures (expired/invalid token),
          // not on network errors or temporary server unavailability.
          const msg = error.message?.toLowerCase() || "";
          if (
            msg.includes("expired") ||
            msg.includes("unauthorized") ||
            msg.includes("invalid token") ||
            msg.includes("401")
          ) {
            setAuthToken(null);
          }
        });
    }
  }, []);

  const login = async (email: string, password: string) => {
    const res = await userApi.login({ email, password });
    const loginData = res.data;

    setAuthToken(loginData.accessToken);
    if (loginData.refreshToken) {
      localStorage.setItem("refreshToken", loginData.refreshToken);
    }
    const userData = {
      id: loginData.userId,
      firstName: loginData.firstName,
      lastName: loginData.lastName,
      email: loginData.email,
      role: loginData.role,
    };
    setUser(userData);
    return userData;
  };

  const register = async (
    firstName: string,
    lastName: string,
    email: string,
    password: string,
  ) => {
    const res = await userApi.register({
      firstName,
      lastName,
      email,
      password,
    });
    const registerData = res.data;
    setAuthToken(registerData.accessToken);
    if (registerData.refreshToken) {
      localStorage.setItem("refreshToken", registerData.refreshToken);
    }
    const userData = {
      id: registerData.userId,
      firstName: registerData.firstName,
      lastName: registerData.lastName,
      email: registerData.email,
      role: registerData.role,
    };
    setUser(userData);
    return userData;
  };

  const logout = () => {
    setAuthToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{ user, login, register, logout, isAuthenticated: !!user }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
