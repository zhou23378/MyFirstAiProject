package com.salon.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务分类实体
 * <p>
 * 存储服务项目的分类信息，如剪发、染发、护理等
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_category")
@Schema(description = "服务分类实体")
public class ServiceCategory extends BaseEntity {

    /** 分类名称 */
    @Schema(description = "分类名称", example = "剪发")
    private String name;

    /** 排序号，越小越靠前 */
    @Schema(description = "排序号", example = "1")
    private Integer sort;
}
