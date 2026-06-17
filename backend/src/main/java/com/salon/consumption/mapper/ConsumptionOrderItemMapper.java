package com.salon.consumption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.consumption.entity.ConsumptionOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消费订单明细 Mapper 接口
 * <p>
 * 提供消费订单明细表的基础 CRUD 操作
 * </p>
 */
@Mapper
public interface ConsumptionOrderItemMapper extends BaseMapper<ConsumptionOrderItem> {

}
