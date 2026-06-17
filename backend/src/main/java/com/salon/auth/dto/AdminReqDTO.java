package com.salon.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminReqDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotNull(message = "角色不能为空")
    private String role;
}
