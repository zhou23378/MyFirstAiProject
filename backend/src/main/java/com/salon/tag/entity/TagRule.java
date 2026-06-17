package com.salon.tag.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag_rule")
public class TagRule extends BaseEntity {
    private String name;
    private String tagName;
    private String conditionsJson;
    private Integer enabled;
}
