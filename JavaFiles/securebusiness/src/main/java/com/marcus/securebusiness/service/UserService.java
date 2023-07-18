package com.marcus.securebusiness.service;

import com.marcus.securebusiness.dto.UserDTO;
import com.marcus.securebusiness.form.UpdateForm;
import com.marcus.securebusiness.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserDTO createUser(User user);
    UserDTO getUserByEmail(String email);
    void sendVerificationCode(UserDTO userDTO);

    UserDTO getUser(String email);
    UserDTO verifyCode(String email, String code);

    void resetPassword(String email);

    UserDTO verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    UserDTO verifyAccount(String key);

    UserDTO updateUserDetails(UpdateForm user);

    UserDTO getUserById(Long userId);

    void updatePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword);
    void updatePassword(Long userId, String newPassword, String confirmNewPassword);

    void updateUserRole(Long userId, String roleName);

    void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked);

    UserDTO toggleMfa(String email);

    void updateImage(UserDTO userDTO, MultipartFile image);
}

