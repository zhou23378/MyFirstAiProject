package com.salon.consumption.vo;

import com.salon.consumption.entity.ConsumptionOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "消费订单视图")
public class ConsumptionOrderVO {

    public static ConsumptionOrderVO from(ConsumptionOrder entity) {
        if (entity == null) return null;
        ConsumptionOrderVO vo = new ConsumptionOrderVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "支付方式：1=现金 2=余额 3=微信 4=混合")
    private Integer payMethod;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "使用余额")
    private BigDecimal balanceUsed;

    @Schema(description = "获得积分")
    private Integer pointsEarned;

    @Schema(description = "订单状态：1=正常 2=已退款")
    private Integer status;

    @Schema(description = "支付备注")
    private String payRemark;

    @Schema(description = "使用优惠券ID")
    private Long couponId;

    @Schema(description = "优惠券优惠金额")
    private BigDecimal couponDiscount;

    @Schema(description = "服务技师ID")
    private Long employeeId;

    @Schema(description = "提成金额")
    private BigDecimal commissionAmount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "订单明细列表")
    private List<ConsumptionOrderItemVO> items;
}
