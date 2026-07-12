import apiClient from "./apiClient";
import { USE_MOCK_DATA } from "../config/appConfig";

const demoUsers = [
  {
    id: 1,
    name: "Fleet Manager",
    email: "manager@transitops.com",
    password: "Transit@123",
    role: "FLEET_MANAGER",
  },
  {
    id: 2,
    name: "Dispatcher",
    email: "dispatcher@transitops.com",
    password: "Transit@123",
    role: "DISPATCHER",
  },
  {
    id: 3,
    name: "Safety Officer",
    email: "safety@transitops.com",
    password: "Transit@123",
    role: "SAFETY_OFFICER",
  },
  {
    id: 4,
    name: "Financial Analyst",
    email: "finance@transitops.com",
    password: "Transit@123",
    role: "FINANCIAL_ANALYST",
  },
];

const delay = (milliseconds) =>
  new Promise((resolve) => setTimeout(resolve, milliseconds));

export const loginUser = async (credentials) => {
  if (!USE_MOCK_DATA) {
    const response = await apiClient.post("/auth/login", credentials);
    return response.data;
  }

  await delay(700);

  const matchedUser = demoUsers.find(
    (user) =>
      user.email.toLowerCase() === credentials.email.toLowerCase() &&
      user.password === credentials.password,
  );

  if (!matchedUser) {
    throw {
      status: 401,
      message: "Invalid email address or password.",
      fieldErrors: {},
    };
  }

  const { password, ...safeUser } = matchedUser;

  return {
    token: `demo-jwt-token-${safeUser.id}`,
    user: safeUser,
  };
};