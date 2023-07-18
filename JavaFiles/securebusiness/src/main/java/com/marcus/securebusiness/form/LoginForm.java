package com.marcus.securebusiness.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {
    @NotEmpty(message = "Email can not be empty")
    @Email(message = "Invalid email. Please enter a valid email address")
    private String email;
    @NotEmpty(message = "Password can not be empty")
    private String password;
}
