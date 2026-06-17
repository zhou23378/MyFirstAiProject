package com.salon.consumption.vo;

import com.salon.consumption.entity.PaymentDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Data
@Schema(description = "支付明细视图")
public class PaymentDetailVO {

    public static PaymentDetailVO from(PaymentDetail entity) {
        if (entity == null) return null;
        PaymentDetailVO vo = new PaymentDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "支付方式")
    private Integer payMethod;

    @Schema(description = "支付金额")
    private BigDecimal amount;
}
