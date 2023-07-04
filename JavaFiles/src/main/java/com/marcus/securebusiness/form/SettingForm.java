package com.marcus.securebusiness.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingForm {
    @NotNull(message = "enabled can not be empty")
    private Boolean enabled;
    @NotNull(message = "notLocked can not be empty")
    private Boolean notLocked;
}
