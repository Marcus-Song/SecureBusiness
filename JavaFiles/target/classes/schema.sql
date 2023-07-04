##############################################################
#               Author: Marcus Song (Weifeng)                #
#               Date: June 16th, 2023                        #
#               Version: 1.0                                 #
##############################################################

CREATE SCHEMA IF NOT EXISTS safebusiness;

SET NAMES 'UTF8MB4';
SET time_zone = '+08:00';

USE safebusiness;

DROP TABLE IF EXISTS UserRoles;
DROP TABLE IF EXISTS UserEvents;
DROP TABLE IF EXISTS AccountVerifications;
DROP TABLE IF EXISTS ResetPasswordVerifications;
DROP TABLE IF EXISTS TwoFactorVerifications;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Roles;
DROP TABLE IF EXISTS Events;

CREATE TABLE Users
(
   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   first_name VARCHAR(50) NOT NULL,
   last_name VARCHAR(50) NOT NULL,
   email VARCHAR(100) NOT NULL,
   password VARCHAR(255) DEFAULT NULL,
   address VARCHAR(255) DEFAULT NULL,
   phone VARCHAR(30) DEFAULT NULL,
   title VARCHAR(50) DEFAULT NULL,
   bio VARCHAR(255) DEFAULT NULL,
   enabled BOOLEAN DEFAULT FALSE,
   non_locked BOOLEAN DEFAULT TRUE,
   using_mfa BOOLEAN DEFAULT FALSE,
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
   image_url VARCHAR(255) DEFAULT 'https://img.freepik.com/free-icon/user_318-159711.jpg',
   CONSTRAINT UQ_Users_Email UNIQUE (email)
);

CREATE TABLE Roles
(
   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(50) NOT NULL,
   permission VARCHAR(255) DEFAULT NULL,
   CONSTRAINT UQ_Roles_Name UNIQUE (name)
);

CREATE TABLE UserRoles
(
   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   user_id BIGINT UNSIGNED NOT NULL,
   role_id BIGINT UNSIGNED NOT NULL,
   FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
   FOREIGN KEY (role_id) REFERENCES Roles (id) ON DELETE RESTRICT ON UPDATE CASCADE,
   CONSTRAINT UQ_UserRole_User_Id UNIQUE (user_id)
);

CREATE TABLE Events
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL CHECK (type IN ('LOGIN_ATTEMPT', 'LOGIN_ATTEMPT_FAILURE', 'LOGIN_ATTEMPT_SUCCESS', 'PROFILE_UPDATE', 'PROFILE_PICTURE_UPDATE', 'ROLE_UPDATE', 'ACCOUNT_SETTING_UPDATE', 'PASSWORD_UPDATE', 'MFA_UPDATE')),
    description VARCHAR(255) DEFAULT NULL,
    CONSTRAINT UQ_Events_Type UNIQUE (type)
);

CREATE TABLE UserEvents
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    event_id BIGINT UNSIGNED NOT NULL,
    device VARCHAR(100) DEFAULT NULL,
    ip_address VARCHAR(100) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (event_id) REFERENCES Events (id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE AccountVerifications
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    url VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT UQ_AccountVerification_User_Id UNIQUE (user_id),
    CONSTRAINT UQ_AccountVerification_Url UNIQUE (url)
);

CREATE TABLE ResetPasswordVerifications
(
   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   user_id BIGINT UNSIGNED NOT NULL,
   url VARCHAR(255) NOT NULL,
   expiration_date DATETIME NOT NULL,
   FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
   CONSTRAINT UQ_ResetPasswordVerification_User_Id UNIQUE (user_id),
   CONSTRAINT UQ_ResetPasswordVerification_Url UNIQUE (url)
);

CREATE TABLE TwoFactorVerifications
(
   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   user_id BIGINT UNSIGNED NOT NULL,
   code VARCHAR(10) NOT NULL,
   expiration_date DATETIME NOT NULL,
   FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
   CONSTRAINT UQ_TwoFactorVerification_User_Id UNIQUE (user_id),
   CONSTRAINT UQ_TwoFactorVerification_Code UNIQUE (code)
);

USE safebusiness;

SELECT * FROM roles;

INSERT INTO roles (name, permission)
VALUES ("ROLE_USER", "READ:USER, READ:CUSTOMER"),
       ("ROLE_MANAGER", "READ:USER, READ:CUSTOMER, UPDATE:USER, UPDATE:CUSTOMER"),
       ("ROLE_ADMIN", "READ:USER, READ:CUSTOMER, UPDATE:USER, UPDATE:CUSTOMER, CREATE:USER, CREATE:CUSTOMER"),
       ("ROLE_SYSADMIN", "READ:USER, READ:CUSTOMER, UPDATE:USER, UPDATE:CUSTOMER, CREATE:USER, CREATE:CUSTOMER, DELETE:USER, DELETE:CUSTOMER");

SELECT * FROM Events;

INSERT INTO Events (type, description)
VALUES ("LOGIN_ATTEMPT", "User tried to log in"),
       ("LOGIN_ATTEMPT_FAILURE", "User tried to log in and failed"),
       ("LOGIN_ATTEMPT_SUCCESS", "User tried to log in and succeed"),
       ("PROFILE_UPDATE", "User updated profile information"),
       ("PROFILE_PICTURE_UPDATE", "User updated profile image"),
       ("ROLE_UPDATE", "User updated roles and permissions"),
       ("ACCOUNT_SETTING_UPDATE", "User updated account settings"),
       ("PASSWORD_UPDATE", "User updated password"),
       ("MFA_UPDATE", "User updated MFA settings");
