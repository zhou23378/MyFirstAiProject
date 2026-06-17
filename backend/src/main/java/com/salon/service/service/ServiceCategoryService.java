package com.salon.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.service.entity.ServiceCategory;

import java.util.List;

/**
 * 服务分类 Service 接口
 * <p>
 * 定义服务分类的业务方法
 * </p>
 */
public interface ServiceCategoryService extends IService<ServiceCategory> {

    /**
     * 查询所有分类，按排序号升序排列
     *
     * @return 分类列表
     */
    List<ServiceCategory> findAllOrderBySort();
}
