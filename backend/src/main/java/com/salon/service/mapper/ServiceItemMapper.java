package com.salon.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.service.entity.ServiceItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务项目 Mapper 接口
 * <p>
 * 提供服务项目表的基础 CRUD 操作
 * </p>
 */
@Mapper
public interface ServiceItemMapper extends BaseMapper<ServiceItem> {

}
