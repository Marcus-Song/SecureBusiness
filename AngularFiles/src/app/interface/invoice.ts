import { InvoiceStatus } from "../enum/invoice-status.enum";

export interface Invoice{
    id: number;
    invoiceNumber: string;
    service: string[];
    price: number[];
    status: InvoiceStatus;
    total: number;
    createdAt: Date;
}