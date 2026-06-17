package com.salon.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.inventory.entity.StockRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecord> { }
