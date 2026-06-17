package com.salon.memberlevel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.memberlevel.entity.MemberLevel;
import com.salon.memberlevel.mapper.MemberLevelMapper;
import com.salon.memberlevel.service.MemberLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 会员等级 Service 实现
 */
@Service
@RequiredArgsConstructor
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelMapper, MemberLevel> implements MemberLevelService {
}
