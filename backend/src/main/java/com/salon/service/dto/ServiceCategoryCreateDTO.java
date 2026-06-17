package com.salon.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建服务分类请求")
public class ServiceCategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称最长100字符")
    @Schema(description = "分类名称", example = "剪发")
    private String name;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Size(max = 500, message = "备注最长500字符")
    @Schema(description = "备注")
    private String remark;
}
