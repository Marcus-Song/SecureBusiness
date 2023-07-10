import { CustomerStatus } from "../enum/customer-status";
import { Invoice } from "./invoice";

export interface Customer{
    id: number;
    name: string;
    email: string;
    address: string;
    type: string;
    status: CustomerStatus;
    imageUrl: string;
    phone: string;
    createdAt: Date;
    invoices?: Invoice[];
}