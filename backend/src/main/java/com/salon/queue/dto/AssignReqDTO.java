package com.salon.queue.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignReqDTO {
    @NotNull(message = "排队ID不能为空")
    private Long queueId;

    @NotNull(message = "技师ID不能为空")
    private Long employeeId;
}
