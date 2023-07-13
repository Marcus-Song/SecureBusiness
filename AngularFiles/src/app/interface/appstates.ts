import { DataState } from "../enum/datastate.enum"
import { Customer } from "./customer";
import { Events } from "./event";
import { Roles } from "./role";
import { User } from "./user";

export interface LoginState {
    dataState: DataState;
    loginSuccess?: boolean;
    error?: string;
    message?: string;
    usingMfa?: boolean;
    phone?: string;
}

export interface CustomHttpResponse<T> {
    timeStamp: Date;
    statusCode: number;
    status: string;
    message: string;
    reason?: string;
    developerMessage?: string;
    data?: T;
}

export interface Profile {
    user: User;
    events?: Events[];
    roles?: Roles[];
    access_token?: string;
    refresh_token?: string;
}

export interface ProfileState<T> {
    dataState: DataState;
    appData?: T;
    error?: string;
}

export interface Page<T> {
    content: T[];
    totalPages: number; 
    totalElements: number;
    numberOfElements: number;
    size: number;
    number: number;
}

export interface CustomerState {
    user: User;
    customer: Customer;
}

export interface RegisterState {
    dataState: DataState;
    registerSuccess?: boolean;
    error?: string;
    message?: string;
}

export type AccountType = 'account' | 'password';

export interface VerifyState {
    dataState: DataState;
    verifySuccess?: boolean;
    error?: string;
    message?: string;
    title?: string;
    type?: AccountType;
}