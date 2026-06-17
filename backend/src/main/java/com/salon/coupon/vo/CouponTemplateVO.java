package com.salon.coupon.vo;

import com.salon.coupon.entity.CouponTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "优惠券模板视图")
public class CouponTemplateVO {

    public static CouponTemplateVO from(CouponTemplate entity) {
        if (entity == null) return null;
        CouponTemplateVO vo = new CouponTemplateVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "满减门槛金额")
    private BigDecimal conditionAmount;

    @Schema(description = "优惠金额/折扣")
    private BigDecimal discountValue;

    @Schema(description = "有效天数")
    private Integer validDays;

    @Schema(description = "总发行量")
    private Integer totalQty;

    @Schema(description = "已发行量")
    private Integer issuedQty;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
