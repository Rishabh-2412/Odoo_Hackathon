import axios from "axios";
import { API_BASE_URL, STORAGE_KEYS } from "../config/appConfig";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(STORAGE_KEYS.token);

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error),
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const responseData = error.response?.data;

    let message = "Something went wrong. Please try again.";

    switch (status) {
      case 400:
        message =
          responseData?.message ||
          "The submitted information is invalid.";
        break;

      case 401:
        message = "Your session has expired. Please sign in again.";

        localStorage.removeItem(STORAGE_KEYS.token);
        localStorage.removeItem(STORAGE_KEYS.user);

        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }
        break;

      case 403:
        message = "You do not have permission to perform this action.";
        break;

      case 404:
        message = "The requested resource was not found.";
        break;

      case 500:
        message = "The server encountered an error.";
        break;

      default:
        if (!error.response) {
          message =
            "Unable to connect to the server. Check whether the backend is running.";
        }
    }

    return Promise.reject({
      status,
      message,
      fieldErrors: responseData?.errors || {},
      originalError: error,
    });
  },
);

export default apiClient;