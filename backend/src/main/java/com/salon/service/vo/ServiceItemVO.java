package com.salon.service.vo;

import com.salon.service.entity.ServiceItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "服务项目视图")
public class ServiceItemVO {

    public static ServiceItemVO from(ServiceItem entity) {
        if (entity == null) return null;
        ServiceItemVO vo = new ServiceItemVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "项目名称")
    private String name;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "时长(分钟)")
    private Integer duration;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态：0=下架 1=上架")
    private Integer status;

    @Schema(description = "提成类型：0=无 1=固定金额 2=比例")
    private Integer commissionType;

    @Schema(description = "提成值（金额或比例）")
    private BigDecimal commissionValue;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
