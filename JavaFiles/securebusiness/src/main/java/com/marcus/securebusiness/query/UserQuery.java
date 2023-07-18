package com.marcus.securebusiness.query;

public class UserQuery {
    // "INSERT INTO Users (first_name, last_name, email, password) VALUES (?, ?, ?, ?)"
    public static final String INSERT_USER_QUERY = "INSERT INTO Users (first_name, last_name, email, password) VALUES (:firstName, :lastName, :email, :password)";
    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM Users WHERE email =:email";
    public static final String INSERT_VERIFICATION_QUERY = "INSERT INTO AccountVerifications (user_id, url) VALUES (:userId, :url)";
    public static final String SELECT_USER_BY_EMAIL_QUERY = "SELECT * FROM Users WHERE email = :email";
    public static final String SELECT_USER_BY_ID_QUERY = "SELECT * FROM Users WHERE id = :id";
    public static final String DELETE_VERIFICATION_CODE_BY_USER_ID = "DELETE FROM TwoFactorVerifications WHERE user_id = :id";
    public static final String INSERT_VERIFICATION_CODE_QUERY = "INSERT INTO TwoFactorVerifications (user_id, code, expiration_date) VALUES (:userId, :code, :expirationDate)";
    public static final String SELECT_USER_BY_USER_CODE_QUERY = "SELECT * FROM Users WHERE id = (SELECT user_id FROM TwoFactorVerifications WHERE code = :code)";
    public static final String DELETE_CODE = "DELETE FROM TwoFactorVerifications WHERE code = :code";
    public static final String SELECT_CODE_EXPIRATION_QUERY = "SELECT expiration_date < NOW() As is_expired FROM TwoFactorVerifications WHERE code = :code";
    public static final String DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY = "DELETE FROM ResetPasswordVerifications WHERE user_id = :userId";
    public static final String INSERT_PASSWORD_VERIFIACTION_QUERY = "INSERT INTO ResetPasswordVerifications (user_id, url, expiration_date) VALUE (:userId, :url, :expirationDate)";
    public static final String SELECT_EXPIRATION_BY_URL = "SELECT expiration_date < NOW() As is_expired FROM ResetPasswordVerifications WHERE url = :url";
    public static final String SELECT_USER_BY_PASSWORD_URL_QUERY = "SELECT * FROM Users Where id = (SELECT user_id FROM ResetPasswordVerifications WHERE url = :url)";
    public static final String DELETE_USER_FROM_PASSWORD_VERIFICATION_QUERY = "DELETE FROM ResetPasswordVerifications WHERE id = :id";
    public static final String DELETE_VERIFICATION_BY_URL_QUERY = "DELETE FROM ResetPasswordVerifications WHERE url = :url";
    public static final String UPDATE_USER_PASSWORD_BY_URL_QUERY = "UPDATE Users SET password = :password WHERE id = (SELECT user_id FROM ResetPasswordVerifications WHERE url = :url)";
    public static final String UPDATE_USER_PASSWORD_BY_ID_QUERY = "UPDATE Users SET password = :password WHERE id = :id";
    public static final String SELECT_USER_BY_ACCOUNT_KEY_QUERY = "SELECT * FROM Users where id = (SELECT user_id FROM AccountVerifications WHERE url = :key)";
    public static final String UPDATE_USER_ENABLED_QUERY = "UPDATE Users SET enabled = :enabled WHERE id = :id";
    public static final String UPDATE_USER_DETAILS_QUERY = "UPDATE Users SET first_name = :firstName, last_name = :lastName, email = :email, phone = :phone, address = :address, title = :title, bio = :bio WHERE id = :id";
    public static final String UPDATE_USER_SETTINGS_BY_ID_QUERY = "UPDATE Users SET enabled = :enabled, non_locked = :nonLocked WHERE id = :id";
    public static final String TOGGLE_USER_MFA_QUERY = "UPDATE Users SET using_mfa = :isUsingMfa WHERE email = :email";
    public static final String UPDATE_USER_IMAGE_QUERY = "UPDATE Users SET image_url = :imageUrl WHERE id = :id";

}
