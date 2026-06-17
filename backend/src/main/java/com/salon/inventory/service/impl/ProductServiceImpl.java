package com.salon.inventory.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.inventory.entity.Product;
import com.salon.inventory.mapper.ProductMapper;
import com.salon.inventory.service.ProductService;
import com.salon.inventory.vo.ProductPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<ProductPageVO> pageWithNames(int page, int pageSize, Long categoryId, String keyword) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (categoryId != null) {
            where.append(" AND p.category_id = ?");
            params.add(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND p.name LIKE ?");
            params.add("%" + keyword + "%");
        }

        String countSql = "SELECT COUNT(*) FROM product p WHERE 1=1" + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);
        String dataSql = "SELECT p.id, p.category_id AS categoryId, p.supplier_id AS supplierId, " +
                "p.name, p.unit, p.sale_price AS salePrice, p.stock_qty AS stockQty, " +
                "p.alert_qty AS alertQty, p.status, p.remark, " +
                "p.create_time AS createTime, p.update_time AS updateTime, " +
                "pc.name AS categoryName, s.name AS supplierName " +
                "FROM product p " +
                "LEFT JOIN product_category pc ON p.category_id = pc.id " +
                "LEFT JOIN supplier s ON p.supplier_id = s.id " +
                "WHERE 1=1" + where +
                " ORDER BY p.id DESC LIMIT ?, ?";
        List<ProductPageVO> list = jdbcTemplate.query(dataSql, new BeanPropertyRowMapper<>(ProductPageVO.class), params.toArray());

        Page<ProductPageVO> result = new Page<>(page, pageSize);
        result.setTotal(total != null ? total : 0);
        result.setRecords(list);
        return result;
    }

    @Override
    public Page<ProductPageVO> lowStock(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        String countSql = "SELECT COUNT(*) FROM product WHERE status = 1 AND stock_qty <= alert_qty";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);

        String dataSql = "SELECT p.id, p.category_id AS categoryId, p.supplier_id AS supplierId, " +
                "p.name, p.unit, p.sale_price AS salePrice, p.stock_qty AS stockQty, " +
                "p.alert_qty AS alertQty, p.status, p.remark, " +
                "p.create_time AS createTime, p.update_time AS updateTime, " +
                "pc.name AS categoryName, s.name AS supplierName " +
                "FROM product p " +
                "LEFT JOIN product_category pc ON p.category_id = pc.id " +
                "LEFT JOIN supplier s ON p.supplier_id = s.id " +
                "WHERE p.status = 1 AND p.stock_qty <= p.alert_qty " +
                "ORDER BY p.stock_qty ASC LIMIT ?, ?";
        List<ProductPageVO> list = jdbcTemplate.query(dataSql, new BeanPropertyRowMapper<>(ProductPageVO.class), offset, pageSize);

        Page<ProductPageVO> result = new Page<>(page, pageSize);
        result.setTotal(total != null ? total : 0);
        result.setRecords(list);
        return result;
    }
}
