package com.salon.rating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "服务评价请求")
public class ServiceRatingReqDTO {

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    @Schema(description = "评分 1-5")
    private Integer rating;

    @Schema(description = "评价标签 JSON，如 [\"态度好\",\"技术好\"]")
    private String tags;

    @Schema(description = "评价内容")
    private String comment;
}
