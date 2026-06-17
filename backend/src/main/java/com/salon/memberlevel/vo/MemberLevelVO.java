package com.salon.memberlevel.vo;

import com.salon.memberlevel.entity.MemberLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "会员等级视图")
public class MemberLevelVO {

    public static MemberLevelVO from(MemberLevel entity) {
        if (entity == null) return null;
        MemberLevelVO vo = new MemberLevelVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "等级名称")
    private String name;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "所需积分")
    private Integer pointsRequired;

    @Schema(description = "状态：0=禁用 1=启用")
    private Integer status;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
