package com.marcus.securebusiness.service;

import com.marcus.securebusiness.enumeration.VerificationType;

public interface EmailService {
    void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType verificationType);
}
