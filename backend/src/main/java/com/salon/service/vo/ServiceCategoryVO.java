package com.salon.service.vo;

import com.salon.service.entity.ServiceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "服务分类视图")
public class ServiceCategoryVO {

    public static ServiceCategoryVO from(ServiceCategory entity) {
        if (entity == null) return null;
        ServiceCategoryVO vo = new ServiceCategoryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
