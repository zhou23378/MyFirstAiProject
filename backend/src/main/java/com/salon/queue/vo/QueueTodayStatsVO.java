package com.salon.queue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "今日排队统计视图")
public class QueueTodayStatsVO {

    @Schema(description = "等待中数量")
    private Integer waiting;

    @Schema(description = "已分配数量")
    private Integer assigned;

    @Schema(description = "总数量")
    private Integer total;
}
