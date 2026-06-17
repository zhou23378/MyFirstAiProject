package com.salon.consumption.vo;

import com.salon.consumption.entity.ConsumptionOrderItem;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Data
public class ConsumptionOrderItemVO {

    public static ConsumptionOrderItemVO from(ConsumptionOrderItem entity) {
        if (entity == null) return null;
        ConsumptionOrderItemVO vo = new ConsumptionOrderItemVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private Long id;
    private Long orderId;
    private Long itemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
}
