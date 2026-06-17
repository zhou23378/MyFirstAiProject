package com.salon.consumption.vo;

import com.salon.consumption.entity.ConsumptionOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "挂起订单摘要")
public class SuspendedOrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "服务项目数")
    private Integer itemCount;

    @Schema(description = "挂单时间")
    private LocalDateTime suspendTime;

    public static SuspendedOrderVO from(ConsumptionOrder order, int itemCount) {
        SuspendedOrderVO vo = new SuspendedOrderVO();
        vo.setId(order.getId());
        vo.setMemberId(order.getMemberId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setItemCount(itemCount);
        vo.setSuspendTime(order.getCreateTime());
        return vo;
    }
}
