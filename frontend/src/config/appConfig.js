export const APP_NAME = "TransitOps";

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

export const USE_MOCK_DATA =
  String(import.meta.env.VITE_USE_MOCK_DATA).toLowerCase() === "true";

export const STORAGE_KEYS = {
  token: "transitops_token",
  user: "transitops_user",
};