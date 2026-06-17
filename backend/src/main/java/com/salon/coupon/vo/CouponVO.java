package com.salon.coupon.vo;

import com.salon.coupon.entity.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "优惠券视图")
public class CouponVO {

    public static CouponVO from(Coupon entity) {
        if (entity == null) return null;
        CouponVO vo = new CouponVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "券码")
    private String code;

    @Schema(description = "面额")
    private BigDecimal value;

    @Schema(description = "满减门槛")
    private BigDecimal minAmount;

    @Schema(description = "状态(0=未使用 1=已使用 2=已过期)")
    private Integer status;

    @Schema(description = "核销时间")
    private LocalDateTime usedAt;

    @Schema(description = "到期日")
    private LocalDate expireDate;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
