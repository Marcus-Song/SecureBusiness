export interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    address?: string;
    phone?: string;
    title?: string;
    bio?: string;
    imageUrl?: string;
    enabled: boolean;
    notLocked: boolean;
    usingMfa: boolean;
    createAt?: Date;
    roleName: string;
    permission: string;
}
