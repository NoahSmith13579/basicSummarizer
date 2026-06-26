import axios, {type AxiosResponse} from "axios";

interface TokenResponse {
    token: string;
}

const baseURL = import.meta.env.VITE_NODE_ENV == "development" ?
    import.meta.env.VITE_DEV_API_URL:
    import.meta.env.VITE_PROD_API_URL

const clientAPI = axios.create({
    baseURL: baseURL + "api/v1/",
    withCredentials: true
})

// Attach token on every request
clientAPI.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('token');
    if(token) config.headers.Authorization = `Bearer ${token}`;
    return config;
})

export async function login(username?: string, password?: string){


    const res: AxiosResponse<TokenResponse>= await clientAPI.post("auth/login",
        {
            username: username ?? "testuser",
            password: password ?? "password"
        }
    );
    sessionStorage.setItem("token", res.data.token)
}

export default clientAPI;