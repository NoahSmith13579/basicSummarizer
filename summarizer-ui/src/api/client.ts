import axios from "axios";

const baseURL =
  import.meta.env.VITE_NODE_ENV == "development"
    ? import.meta.env.VITE_DEV_API_URL
    : import.meta.env.VITE_PROD_API_URL;

const clientAPI = axios.create({
  baseURL: baseURL + "api/v1/",
});

export default clientAPI;
