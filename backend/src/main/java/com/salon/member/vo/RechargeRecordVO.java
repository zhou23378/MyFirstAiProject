package com.salon.member.vo;

import com.salon.member.entity.RechargeRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "充值记录视图")
public class RechargeRecordVO {

    public static RechargeRecordVO from(RechargeRecord entity) {
        if (entity == null) return null;
        RechargeRecordVO vo = new RechargeRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

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
