export interface resetPassword {
    currentPassword: string;
    newPassword: string;
    confirmNewPassword: string;
}

export interface renewPassword {
    userId: number;
    password: string;
    confirmPassword: string;
}