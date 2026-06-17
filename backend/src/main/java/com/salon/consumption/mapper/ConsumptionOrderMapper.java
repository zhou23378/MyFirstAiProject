package com.salon.consumption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.consumption.entity.ConsumptionOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消费订单 Mapper 接口
 * <p>
 * 提供消费订单表的基础 CRUD 操作
 * </p>
 */
@Mapper
public interface ConsumptionOrderMapper extends BaseMapper<ConsumptionOrder> {

}
