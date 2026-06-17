package com.salon.tag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.tag.entity.TagRule;
import com.salon.tag.mapper.TagRuleMapper;
import com.salon.tag.service.TagRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagRuleServiceImpl extends ServiceImpl<TagRuleMapper, TagRule> implements TagRuleService {
}
