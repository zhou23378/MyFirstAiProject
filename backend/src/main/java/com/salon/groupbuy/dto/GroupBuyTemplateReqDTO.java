package com.salon.groupbuy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyTemplateReqDTO {

    @NotBlank(message = "活动名称不能为空")
    private String name;

    private String imageUrl;
    private String description;

    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    @NotNull(message = "拼团价不能为空")
    @DecimalMin(value = "0.01", message = "拼团价必须大于0")
    private BigDecimal groupPrice;

    @NotNull(message = "成团人数不能为空")
    @Min(value = 2, message = "成团人数至少为2")
    private Integer groupSize;

    @NotNull(message = "有效时长不能为空")
    @Min(value = 1, message = "有效时长至少1小时")
    @Max(value = 720, message = "有效时长最多720小时")
    private Integer expireHours;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer totalQty;
    private Integer status;
    private Integer sortOrder;
}
