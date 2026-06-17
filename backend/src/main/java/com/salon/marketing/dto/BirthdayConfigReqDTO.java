package com.salon.marketing.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "生日营销配置请求")
public class BirthdayConfigReqDTO {

    @Min(value = 0, message = "启用状态只能为0或1")
    @Max(value = 1, message = "启用状态只能为0或1")
    @Schema(description = "0=停用 1=启用", example = "1")
    private Integer enabled;

    @Schema(description = "发放的优惠券模板ID，可为空")
    private Long couponTemplateId;

    @JsonIgnore
    private boolean couponTemplateIdPresent;

    @Min(value = 0, message = "短信开关只能为0或1")
    @Max(value = 1, message = "短信开关只能为0或1")
    @Schema(description = "0=不发短信 1=发送短信", example = "1")
    private Integer smsEnabled;

    public void setCouponTemplateId(Long couponTemplateId) {
        this.couponTemplateId = couponTemplateId;
        this.couponTemplateIdPresent = true;
    }

    @JsonIgnore
    public boolean hasCouponTemplateId() {
        return couponTemplateIdPresent;
    }
}
