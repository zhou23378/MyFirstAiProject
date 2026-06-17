package com.salon.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 会员新增/修改请求 DTO
 * <p>
 * 包含会员的完整信息，带有参数校验注解
 * </p>
 */
@Data
@Schema(description = "会员新增/修改请求")
public class MemberReqDTO {

    /** 会员姓名，必填 */
    @NotBlank(message = "会员姓名不能为空")
    @Schema(description = "会员姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /** 手机号，格式校验 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /** 性别：0=未知 1=男 2=女 */
    @Schema(description = "性别：0=未知 1=男 2=女", example = "1")
    private Integer gender;

    /** 会员等级 */
    @Schema(description = "会员等级", example = "1")
    private Integer level;

    /** 积分 */
    @Schema(description = "积分", example = "100")
    private Integer points;

    /** 余额 */
    @PositiveOrZero(message = "余额不能为负数")
    @Schema(description = "余额", example = "99.50")
    private BigDecimal balance;

    /** 会员标签 */
    @Schema(description = "会员标签", example = "老客,高消费")
    private String tags;

    /** 生日 */
    @Schema(description = "生日", example = "1990-05-20")
    private LocalDate birthday;

    /** 生日类型：1=公历 2=农历 */
    @Schema(description = "生日类型：1=公历 2=农历", example = "1")
    private Integer birthdayType;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
