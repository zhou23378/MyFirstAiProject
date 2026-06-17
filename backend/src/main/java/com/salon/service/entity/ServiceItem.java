package com.salon.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 服务项目实体
 * <p>
 * 存储具体的服务项目信息，如洗剪吹、染发等
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_item")
@Schema(description = "服务项目实体")
public class ServiceItem extends BaseEntity {

    /** 所属分类ID */
    @Schema(description = "所属分类ID", example = "1")
    private Long categoryId;

    /** 项目名称 */
    @Schema(description = "项目名称", example = "洗剪吹")
    private String name;

    /** 价格 */
    @Schema(description = "价格", example = "68.00")
    private BigDecimal price;

    /** 时长（分钟） */
    @Schema(description = "时长（分钟）", example = "30")
    private Integer duration;

    /** 状态：0=下架 1=上架 */
    @Schema(description = "状态：0=下架 1=上架", example = "1")
    private Integer status;

    /** 提成类型：0=无 1=固定金额 2=比例 */
    @Schema(description = "提成类型：0=无 1=固定金额 2=比例", example = "1")
    private Integer commissionType;

    /** 提成值（金额或比例） */
    @Schema(description = "提成值（金额或比例）", example = "10.00")
    private BigDecimal commissionValue;
}
