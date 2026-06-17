package com.salon.queue.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnqueueReqDTO {
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotNull(message = "会员姓名不能为空")
    private String memberName;

    private Long serviceItemId;
    private String serviceItemName;
}
