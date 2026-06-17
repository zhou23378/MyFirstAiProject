package com.salon.points.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.points.dto.PointsProductReqDTO;
import com.salon.points.entity.PointsProduct;
import com.salon.points.vo.PointsProductPageVO;
import com.salon.points.vo.PointsProductVO;

public interface PointsProductService extends IService<PointsProduct> {

    Page<PointsProductPageVO> page(int page, int size, String name, Integer status);

    PointsProductVO getById(Long id);

    PointsProductVO create(PointsProductReqDTO dto);

    PointsProductVO update(Long id, PointsProductReqDTO dto);

    void updateStatus(Long id, Integer status);
}
