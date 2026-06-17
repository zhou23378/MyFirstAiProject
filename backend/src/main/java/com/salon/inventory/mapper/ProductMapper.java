package com.salon.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.inventory.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> { }
