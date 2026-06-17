package com.salon.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 会员实体
 * <p>
 * 存储会员基本信息，包括姓名、手机号、性别、等级、积分、余额等
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member")
@Schema(description = "会员实体")
public class Member extends BaseEntity {

    /** 会员姓名 */
    @Schema(description = "会员姓名", example = "张三")
    private String name;

    /** 手机号 */
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
    @Schema(description = "余额", example = "99.50")
    private BigDecimal balance;

    /** 会员标签，逗号分隔 */
    @Schema(description = "会员标签", example = "老客,高消费,烫染偏好")
    private String tags;

    /** 生日 */
    @Schema(description = "生日", example = "1990-05-20")
    private LocalDate birthday;

    /** 生日类型：1=公历 2=农历 */
    @Schema(description = "生日类型：1=公历 2=农历", example = "1")
    private Integer birthdayType;

    /** 最后消费日期 */
    @Schema(description = "最后消费日期", example = "2026-05-15")
    private LocalDate lastConsumeDate;

    /** 最后到店日期 */
    @Schema(description = "最后到店日期")
    private LocalDate lastVisitDate;

    /** 累计消费金额 */
    @Schema(description = "累计消费金额")
    private BigDecimal totalSpent;

    /** 到店次数 */
    @Schema(description = "到店次数")
    private Integer visitCount;

    /** RFM分层 */
    @Schema(description = "RFM分层: CHAMPIONS|LOYAL|POTENTIAL|AT_RISK|LOST")
    private String rfmSegment;

    /** 顾客登录密码(BCrypt) */
    @Schema(description = "顾客登录密码(BCrypt)")
    private String password;

    /** 微信OpenID */
    @Schema(description = "微信OpenID")
    private String openid;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}
