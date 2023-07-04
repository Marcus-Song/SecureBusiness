package com.marcus.securebusiness.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateForm {
    private Long id;
    @NotEmpty(message = "firstName can not be empty")
    private String firstName;
    @NotEmpty(message = "lastName can not be empty")
    private String lastName;
    @NotEmpty(message = "Email can not be empty")
    @Email(message = "Invalid email. Please enter a valid email address")
    private String email;
    @Pattern(regexp = "^\\d{10}$", message="Invalid phone number")
    private String phone;
    private String address;
    private String title;
    private String bio;
}
