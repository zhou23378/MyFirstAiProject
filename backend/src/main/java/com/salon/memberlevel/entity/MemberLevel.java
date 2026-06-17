package com.salon.memberlevel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 会员等级实体
 * <p>
 * 定义会员等级名称、积分门槛和折扣率
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_level")
@Schema(description = "会员等级实体")
public class MemberLevel extends BaseEntity {

    /** 等级名称 */
    @Schema(description = "等级名称", example = "黄金会员")
    private String name;

    /** 所需积分门槛 */
    @Schema(description = "所需积分门槛", example = "500")
    private Integer pointsRequired;

    /** 折扣率（如 0.9 表示 9 折） */
    @Schema(description = "折扣率", example = "0.90")
    private BigDecimal discountRate;

    /** 排序号 */
    @Schema(description = "排序号", example = "1")
    private Integer sort;

    /** 状态：0=禁用 1=启用 */
    @Schema(description = "状态：0=禁用 1=启用", example = "1")
    private Integer status;
}
