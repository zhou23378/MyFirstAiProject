package com.salon.schedule.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.schedule.entity.ShiftTemplate;
import com.salon.schedule.mapper.ShiftTemplateMapper;
import com.salon.schedule.service.ShiftTemplateService;
import org.springframework.stereotype.Service;

@Service
public class ShiftTemplateServiceImpl extends ServiceImpl<ShiftTemplateMapper, ShiftTemplate> implements ShiftTemplateService { }
