package com.salon.queue.vo;

import com.salon.queue.entity.ServiceQueue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "轮牌排队视图")
public class ServiceQueueVO {

    public static ServiceQueueVO from(ServiceQueue entity) {
        if (entity == null) return null;
        ServiceQueueVO vo = new ServiceQueueVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员名称")
    private String memberName;

    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @Schema(description = "服务项目名称")
    private String serviceItemName;

    @Schema(description = "排队号")
    private Integer queueNumber;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "分配技师ID")
    private Long assignedEmployeeId;

    @Schema(description = "等待开始时间")
    private LocalDateTime waitSince;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
