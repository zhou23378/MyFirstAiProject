package com.salon.tag.vo;

import com.salon.tag.entity.TagRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "标签规则视图")
public class TagRuleVO {

    public static TagRuleVO from(TagRule entity) {
        if (entity == null) return null;
        TagRuleVO vo = new TagRuleVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "条件JSON")
    private String conditionsJson;

    @Schema(description = "启用状态")
    private Integer enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
