package com.salon.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.auth.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员 Mapper 接口
 * <p>
 * 提供管理员表的基础 CRUD 操作
 * </p>
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

}
