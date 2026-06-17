package com.salon.customer.vo;

import com.salon.member.entity.RechargeRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "余额变动记录")
public class BalanceRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "金额（正数=充值，负数=消费）")
    private BigDecimal amount;

    @Schema(description = "变动类型: RECHARGE(充值) / CONSUME(消费)")
    private String type;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public static BalanceRecordVO from(RechargeRecord record) {
        BalanceRecordVO vo = new BalanceRecordVO();
        vo.setId(record.getId());
        vo.setAmount(record.getAmount());
        vo.setType("RECHARGE");
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
