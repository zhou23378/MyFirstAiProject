package com.salon.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "标签规则请求")
public class TagRuleReqDTO {

    @NotBlank(message = "规则名称不能为空")
    @Schema(description = "规则名称", example = "流失风险会员")
    private String name;

    @NotBlank(message = "标签名不能为空")
    @Schema(description = "生成的标签名", example = "流失风险")
    private String tagName;

    @NotBlank(message = "条件JSON不能为空")
    @Schema(description = "条件JSON", example = "[{\"field\":\"last_visit_days\",\"op\":\">\",\"value\":60}]")
    private String conditionsJson;

    @Schema(description = "是否启用", example = "1")
    private Integer enabled;
}
