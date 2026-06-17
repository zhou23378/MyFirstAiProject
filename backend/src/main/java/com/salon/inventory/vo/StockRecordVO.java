package com.salon.inventory.vo;

import com.salon.inventory.entity.StockRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "出入库记录视图")
public class StockRecordVO {

    public static StockRecordVO from(StockRecord entity) {
        if (entity == null) return null;
        StockRecordVO vo = new StockRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "类型(1=入库/2=出库)")
    private Integer type;

    @Schema(description = "数量")
    private Integer qty;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
