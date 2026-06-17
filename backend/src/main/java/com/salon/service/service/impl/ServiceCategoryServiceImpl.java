package com.salon.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.service.entity.ServiceCategory;
import com.salon.service.mapper.ServiceCategoryMapper;
import com.salon.service.service.ServiceCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务分类 Service 实现
 */
@Slf4j
@Service
public class ServiceCategoryServiceImpl extends ServiceImpl<ServiceCategoryMapper, ServiceCategory> implements ServiceCategoryService {

    /**
     * 查询所有分类，按排序号升序排列
     */
    @Override
    public List<ServiceCategory> findAllOrderBySort() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<ServiceCategory>()
                        .orderByAsc(ServiceCategory::getSort));
    }

    @Override
    @Transactional
    public boolean save(ServiceCategory entity) {
        boolean result = super.save(entity);
        if (result) {
            log.info("新增服务分类成功: id={}, name={}", entity.getId(), entity.getName());
        }
        return result;
    }

    @Override
    @Transactional
    public boolean updateById(ServiceCategory entity) {
        boolean result = super.updateById(entity);
        if (result) {
            log.info("修改服务分类成功: id={}, name={}", entity.getId(), entity.getName());
        }
        return result;
    }

    @Override
    @Transactional
    public boolean removeById(ServiceCategory entity) {
        boolean result = super.removeById(entity);
        if (result) {
            log.info("删除服务分类成功: id={}", entity.getId());
        }
        return result;
    }
}
