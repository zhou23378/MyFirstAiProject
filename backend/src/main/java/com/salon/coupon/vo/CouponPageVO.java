package com.salon.coupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "优惠券分页视图")
public class CouponPageVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "优惠券模板ID")
    private Long templateId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "优惠券编码")
    private String code;

    @Schema(description = "优惠券模板名称")
    private String templateName;

    @Schema(description = "优惠券模板类型")
    private String templateType;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "状态：1=未使用 2=已使用 3=已过期")
    private Integer status;

    @Schema(description = "优惠金额")
    private BigDecimal discountValue;

    @Schema(description = "使用门槛金额")
    private BigDecimal conditionAmount;

    @Schema(description = "领取时间")
    private LocalDateTime receiveTime;

    @Schema(description = "使用时间")
    private LocalDateTime useTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
