package com.salon.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.service.entity.ServiceItem;
import com.salon.service.mapper.ServiceItemMapper;
import com.salon.service.service.ServiceItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务项目 Service 实现
 */
@Slf4j
@Service
public class ServiceItemServiceImpl extends ServiceImpl<ServiceItemMapper, ServiceItem> implements ServiceItemService {

    /**
     * 根据分类ID查询项目列表
     */
    @Override
    public List<ServiceItem> findByCategoryId(Long categoryId) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<ServiceItem>()
                        .eq(ServiceItem::getCategoryId, categoryId)
                        .orderByAsc(ServiceItem::getCreateTime));
    }

    @Override
    @Transactional
    public boolean save(ServiceItem entity) {
        boolean result = super.save(entity);
        if (result) {
            log.info("新增服务项目成功: id={}, name={}, price={}", entity.getId(), entity.getName(), entity.getPrice());
        }
        return result;
    }

    @Override
    @Transactional
    public boolean updateById(ServiceItem entity) {
        boolean result = super.updateById(entity);
        if (result) {
            log.info("修改服务项目成功: id={}, name={}", entity.getId(), entity.getName());
        }
        return result;
    }

    @Override
    @Transactional
    public boolean removeById(ServiceItem entity) {
        boolean result = super.removeById(entity);
        if (result) {
            log.info("删除服务项目成功: id={}", entity.getId());
        }
        return result;
    }
}
