package com.salon.tag.vo;

import com.salon.tag.entity.MemberTag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "会员标签视图")
public class MemberTagVO {

    public static MemberTagVO from(MemberTag entity) {
        if (entity == null) return null;
        MemberTagVO vo = new MemberTagVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "来源(AUTO/MANUAL)")
    private String source;

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
