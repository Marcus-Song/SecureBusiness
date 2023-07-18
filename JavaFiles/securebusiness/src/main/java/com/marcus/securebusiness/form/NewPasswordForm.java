package com.marcus.securebusiness.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPasswordForm {

    private Long userId;
    @NotEmpty(message = "Password can not be empty")
    private String password;
    @NotEmpty(message = "Confirm Password can not be empty")
    private String confirmPassword;
}
