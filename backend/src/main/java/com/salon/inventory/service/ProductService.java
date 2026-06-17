package com.salon.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.inventory.entity.Product;
import com.salon.inventory.vo.ProductPageVO;

public interface ProductService extends IService<Product> {

    Page<ProductPageVO> pageWithNames(int page, int pageSize, Long categoryId, String keyword);

    /** 库存量 <= 预警阈值的商品 */
    Page<ProductPageVO> lowStock(int page, int pageSize);
}
