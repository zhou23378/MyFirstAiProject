package com.salon.schedule.vo;

import com.salon.schedule.entity.ShiftTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "班次模板视图")
public class ShiftTemplateVO {

    public static ShiftTemplateVO from(ShiftTemplate entity) {
        if (entity == null) return null;
        ShiftTemplateVO vo = new ShiftTemplateVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
