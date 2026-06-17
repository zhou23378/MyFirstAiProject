package com.salon.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salon.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员 Mapper 接口
 * <p>
 * 提供会员表的基础 CRUD 操作，由 MyBatis Plus 自动实现
 * </p>
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {

}
