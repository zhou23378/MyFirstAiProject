package com.salon.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "顾客登录响应")
public class CustomerLoginRespDTO {
    @Schema(description = "会话Token", example = "a1b2c3d4-e5f6-...")
    private String token;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;
}
