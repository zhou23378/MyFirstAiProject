package com.salon.consumption.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 消费订单明细实体
 * <p>
 * 存储订单中的每个服务项目明细
 * </p>
 */
@Data
@TableName("consumption_order_item")
@Schema(description = "消费订单明细实体")
public class ConsumptionOrderItem {

    /** 主键ID */
    @Schema(description = "主键ID")
    private Long id;

    /** 订单ID */
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    /** 服务项目ID */
    @Schema(description = "服务项目ID", example = "1")
    private Long itemId;

    /** 项目名称 */
    @Schema(description = "项目名称", example = "洗剪吹")
    private String itemName;

    /** 项目价格 */
    @Schema(description = "项目价格", example = "68.00")
    private BigDecimal itemPrice;

    /** 数量 */
    @Schema(description = "数量", example = "1")
    private Integer quantity;
}
