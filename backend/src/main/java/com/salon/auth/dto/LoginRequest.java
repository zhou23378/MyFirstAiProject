package com.salon.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 * <p>
 * 封装登录所需的用户名和密码
 * </p>
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    /** 用户名，必填 */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /** 密码，必填 */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
