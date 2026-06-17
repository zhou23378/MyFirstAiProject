package com.salon.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge_record")
@Schema(description = "充值记录实体")
public class RechargeRecord {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "充值金额")
    private BigDecimal amount;

    @Schema(description = "支付方式：1=现金 2=微信 3=支付宝 4=银行卡")
    private Integer payMethod;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "充值时间")
    private LocalDateTime createTime;
}
