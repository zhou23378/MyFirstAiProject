package com.salon.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.service.entity.ServiceCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务分类 Mapper 接口
 * <p>
 * 提供服务分类表的基础 CRUD 操作
 * </p>
 */
@Mapper
public interface ServiceCategoryMapper extends BaseMapper<ServiceCategory> {

}
