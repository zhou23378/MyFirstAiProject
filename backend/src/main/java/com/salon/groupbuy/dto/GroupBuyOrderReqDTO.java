package com.salon.groupbuy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupBuyOrderReqDTO {

    @NotNull(message = "拼团模板ID不能为空")
    private Long templateId;
}
