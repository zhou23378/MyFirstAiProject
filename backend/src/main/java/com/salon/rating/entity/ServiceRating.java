package com.salon.rating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_rating")
@Schema(description = "服务评价")
public class ServiceRating {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "消费订单ID")
    private Long orderId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "评分 1-5")
    private Integer rating;

    @Schema(description = "评价标签 JSON")
    private String tags;

    @Schema(description = "评价内容")
    private String comment;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
