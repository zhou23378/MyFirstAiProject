package com.salon.consumption.vo;

import com.salon.consumption.entity.ServiceCard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "次卡视图")
public class ServiceCardVO {

    public static ServiceCardVO from(ServiceCard entity) {
        if (entity == null) return null;
        ServiceCardVO vo = new ServiceCardVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @Schema(description = "次卡名称")
    private String name;

    @Schema(description = "总次数")
    private Integer totalCount;

    @Schema(description = "已用次数")
    private Integer usedCount;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "到期日")
    private LocalDate expireDate;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
