package com.marcus.securebusiness.enumeration;

import java.util.Locale;

public enum VerificationType {
    ACCOUNT("ACCOUNT"),
    PASSWORD("PASSWORD"),
    ATTACHMENT("ATTACHMENT");

    private final String type;

    VerificationType(String type) {this.type = type;}

    public String getType() {
        return this.type.toLowerCase();
    }
}
