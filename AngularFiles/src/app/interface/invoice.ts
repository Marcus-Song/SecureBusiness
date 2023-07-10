import { InvoiceStatus } from "../enum/invoice-status.enum";

export interface Invoice{
    id: number;
    invoiceNumber: string;
    service: string;
    status: InvoiceStatus;
    total: number;
    createdAt: Date;
}