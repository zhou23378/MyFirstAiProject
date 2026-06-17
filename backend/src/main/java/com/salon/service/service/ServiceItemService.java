package com.salon.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.service.entity.ServiceItem;

import java.util.List;

/**
 * 服务项目 Service 接口
 * <p>
 * 定义服务项目的业务方法
 * </p>
 */
public interface ServiceItemService extends IService<ServiceItem> {

    /**
     * 根据分类ID查询项目列表
     *
     * @param categoryId 分类ID
     * @return 项目列表
     */
    List<ServiceItem> findByCategoryId(Long categoryId);
}
