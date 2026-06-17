package com.salon.tag.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_tag")
public class MemberTag extends BaseEntity {
    private Long memberId;
    private String tagName;
    private String source;
    private Long ruleId;
}
