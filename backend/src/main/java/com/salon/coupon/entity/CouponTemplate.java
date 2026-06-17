package com.salon.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_template")
public class CouponTemplate extends BaseEntity {
    private String name;
    private Integer type;
    private BigDecimal conditionAmount;
    private BigDecimal discountValue;
    private Integer validDays;
    private Integer totalQty;
    private Integer issuedQty;
    private Integer status;
    private String remark;
}
