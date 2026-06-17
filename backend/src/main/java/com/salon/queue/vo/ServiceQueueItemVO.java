package com.salon.queue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务排队项视图")
public class ServiceQueueItemVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @Schema(description = "分配技师ID")
    private Long assignedEmployeeId;

    @Schema(description = "会员名称")
    private String memberName;

    @Schema(description = "服务项目名称")
    private String serviceItemName;

    @Schema(description = "排队号")
    private Integer queueNumber;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "等待开始时间")
    private LocalDateTime waitSince;

    @Schema(description = "等待分钟数")
    private Long waitMinutes;
}
