package com.codecampus.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordDto {

    @NotEmpty(message = "Vui lòng nhập mật khẩu cũ")
    private String oldPassword;

    @NotEmpty(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;

    @NotEmpty(message = "Vui lòng xác nhận mật khẩu mới")
    private String confirmPassword;
}