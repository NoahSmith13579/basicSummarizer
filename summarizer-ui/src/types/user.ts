import type {LocalDateTime} from "./localDateTime";

// User from backend
export interface User{
    id: number,
    username: string,
    email: string,
    password: string,
    createdAt: LocalDateTime
}