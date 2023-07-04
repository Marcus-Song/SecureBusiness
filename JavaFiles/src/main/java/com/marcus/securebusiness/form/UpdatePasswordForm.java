package com.marcus.securebusiness.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordForm {
    @NotEmpty(message = "Current Password can not be empty")
    private String currentPassword;
    @NotEmpty(message = "New Password can not be empty")
    private String newPassword;
    @NotEmpty(message = "Confirm Password can not be empty")
    private String confirmNewPassword;
}
