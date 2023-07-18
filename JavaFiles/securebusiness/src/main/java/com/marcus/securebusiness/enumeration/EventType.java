package com.marcus.securebusiness.enumeration;

public enum EventType {
    LOGIN_ATTEMPT("User tried to log in"),
    LOGIN_ATTEMPT_FAILURE("User tried to log in and failed"),
    LOGIN_ATTEMPT_SUCCESS("User tried to log in and succeed"),
    PROFILE_UPDATE("User updated profile information"),
    PROFILE_PICTURE_UPDATE("User updated profile image"),
    ROLE_UPDATE("User updated roles and permissions"),
    ACCOUNT_SETTING_UPDATE("User updated account settings"),
    PASSWORD_UPDATE("User updated password"),
    MFA_UPDATE("User updated MFA settings");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
