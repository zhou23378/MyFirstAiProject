package com.salon.consumption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("payment_detail")
@Schema(description = "支付明细实体")
public class PaymentDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "支付方式")
    private Integer payMethod;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "第三方交易号")
    private String transactionId;

    @Schema(description = "支付渠道: WECHAT/ALIPAY/CASH/BALANCE")
    private String payChannel;

    @Schema(description = "支付二维码链接")
    private String qrCodeUrl;
}
