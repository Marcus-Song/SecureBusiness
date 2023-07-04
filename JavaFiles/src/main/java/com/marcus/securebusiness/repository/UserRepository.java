package com.marcus.securebusiness.repository;

import com.marcus.securebusiness.dto.UserDTO;
import com.marcus.securebusiness.form.UpdateForm;
import com.marcus.securebusiness.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

public interface UserRepository <T extends User>{
    // Basic BRUD Operations
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);


    // More Complex Operations
    User getUserByEmail(String email);
    void sendVerificationCode(UserDTO userDTO);
    User verifyCode(String email, String code);

    void resetPassword(String email);

    T verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    User verifyAccount(String key);

    T updateUserDetails(UpdateForm user);

    T getUserById(Long userId);

    void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);

    void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked);

    T toggleMfa(String email);

    void updateImage(UserDTO userDTO, MultipartFile image);
}
