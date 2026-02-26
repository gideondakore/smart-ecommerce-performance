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
            role: typeof profileData.role === 'string' ? profileData.role : profileData.role?.toString() || 'CUSTOMER'
          });
        })
        .catch(() => setAuthToken(null));
    }
  }, []);

  const login = async (email: string, password: string) => {
    const res = await userApi.login({ email, password });
    // Backend returns ApiResponse with data wrapper: {status, message, data: {token, ...userData}}
    const loginData = res.data;

    setAuthToken(loginData.token);
    // The user data is in the same object as token
    const userData = {
      id: loginData.id,
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
    // Backend returns ApiResponse with data wrapper: {status, message, data: {token, ...userData}}
    const registerData = res.data;
    setAuthToken(registerData.token);
    // The user data is in the same object as token
    const userData = {
      id: registerData.id,
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
