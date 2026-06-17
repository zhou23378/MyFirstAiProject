package com.salon.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.points.dto.PointsProductReqDTO;
import com.salon.points.entity.PointsProduct;
import com.salon.points.mapper.PointsProductMapper;
import com.salon.points.service.PointsProductService;
import com.salon.points.vo.PointsProductPageVO;
import com.salon.points.vo.PointsProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsProductServiceImpl extends ServiceImpl<PointsProductMapper, PointsProduct>
    implements PointsProductService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<PointsProductPageVO> page(int page, int size, String name, Integer status) {
        LambdaQueryWrapper<PointsProduct> w = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) w.like(PointsProduct::getName, name);
        if (status != null) w.eq(PointsProduct::getStatus, status);
        w.orderByAsc(PointsProduct::getSortOrder).orderByDesc(PointsProduct::getCreateTime);

        Page<PointsProduct> entityPage = baseMapper.selectPage(new Page<>(page, size), w);

        Page<PointsProductPageVO> result = new Page<>(page, size, entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(e -> {
            PointsProductPageVO vo = new PointsProductPageVO();
            vo.setId(e.getId());
            vo.setName(e.getName());
            vo.setImageUrl(e.getImageUrl());
            vo.setPointsPrice(e.getPointsPrice());
            vo.setStockQty(e.getStockQty());
            vo.setExchangedCount(e.getExchangedCount());
            vo.setStatus(e.getStatus());
            vo.setSortOrder(e.getSortOrder());
            vo.setCreateTime(e.getCreateTime());
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    @Override
    public PointsProductVO getById(Long id) {
        PointsProduct entity = baseMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.POINTS_PRODUCT_NOT_FOUND);

        PointsProductVO vo = new PointsProductVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setImageUrl(entity.getImageUrl());
        vo.setPointsPrice(entity.getPointsPrice());
        vo.setOriginalPrice(entity.getOriginalPrice());
        vo.setStockQty(entity.getStockQty());
        vo.setExchangedCount(entity.getExchangedCount());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    @Override
    public PointsProductVO create(PointsProductReqDTO dto) {
        int status = dto.getStatus() != null ? dto.getStatus() : 1;
        int sortOrder = dto.getSortOrder() != null ? dto.getSortOrder() : 0;

        int rows = jdbcTemplate.update(
            "INSERT INTO points_product (name, image_url, points_price, original_price, stock_qty, description, status, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            dto.getName(), dto.getImageUrl(), dto.getPointsPrice(), dto.getOriginalPrice(),
            dto.getStockQty(), dto.getDescription(), status, sortOrder);
        if (rows == 0) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "商品创建失败");

        List<Map<String, Object>> saved = jdbcTemplate.queryForList(
            "SELECT id, name, image_url, points_price, original_price, stock_qty, exchanged_count, description, status, sort_order, create_time, update_time FROM points_product ORDER BY id DESC LIMIT 1");
        if (saved.isEmpty()) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "商品创建后查询失败");

        Map<String, Object> row = saved.get(0);
        PointsProductVO vo = new PointsProductVO();
        vo.setId(((Number) row.get("id")).longValue());
        vo.setName((String) row.get("name"));
        vo.setImageUrl((String) row.get("image_url"));
        vo.setPointsPrice(((Number) row.get("points_price")).intValue());
        vo.setOriginalPrice((BigDecimal) row.get("original_price"));
        vo.setStockQty(((Number) row.get("stock_qty")).intValue());
        vo.setExchangedCount(((Number) row.get("exchanged_count")).intValue());
        vo.setDescription((String) row.get("description"));
        vo.setStatus(((Number) row.get("status")).intValue());
        vo.setSortOrder(((Number) row.get("sort_order")).intValue());
        Object createTime = row.get("create_time");
        if (createTime instanceof LocalDateTime ldt) vo.setCreateTime(ldt);
        else if (createTime instanceof java.sql.Timestamp ts) vo.setCreateTime(ts.toLocalDateTime());
        Object updateTime = row.get("update_time");
        if (updateTime instanceof LocalDateTime ldt) vo.setUpdateTime(ldt);
        else if (updateTime instanceof java.sql.Timestamp ts) vo.setUpdateTime(ts.toLocalDateTime());
        return vo;
    }

    @Override
    public PointsProductVO update(Long id, PointsProductReqDTO dto) {
        int rows = jdbcTemplate.update(
            "UPDATE points_product SET name = ?, image_url = ?, points_price = ?, original_price = ?, stock_qty = ?, description = ?, status = ?, sort_order = ? WHERE id = ?",
            dto.getName(), dto.getImageUrl(), dto.getPointsPrice(), dto.getOriginalPrice(),
            dto.getStockQty(), dto.getDescription(),
            dto.getStatus() != null ? dto.getStatus() : 1,
            dto.getSortOrder() != null ? dto.getSortOrder() : 0,
            id);
        if (rows == 0) throw new BusinessException(ErrorCode.POINTS_PRODUCT_NOT_FOUND);

        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT id, name, image_url, points_price, original_price, stock_qty, exchanged_count, description, status, sort_order, create_time, update_time FROM points_product WHERE id = ?", id);
        PointsProductVO vo = new PointsProductVO();
        vo.setId(((Number) row.get("id")).longValue());
        vo.setName((String) row.get("name"));
        vo.setImageUrl((String) row.get("image_url"));
        vo.setPointsPrice(((Number) row.get("points_price")).intValue());
        vo.setOriginalPrice((BigDecimal) row.get("original_price"));
        vo.setStockQty(((Number) row.get("stock_qty")).intValue());
        vo.setExchangedCount(((Number) row.get("exchanged_count")).intValue());
        vo.setDescription((String) row.get("description"));
        vo.setStatus(((Number) row.get("status")).intValue());
        vo.setSortOrder(((Number) row.get("sort_order")).intValue());
        Object createTime = row.get("create_time");
        if (createTime instanceof LocalDateTime ldt) vo.setCreateTime(ldt);
        else if (createTime instanceof java.sql.Timestamp ts) vo.setCreateTime(ts.toLocalDateTime());
        Object updateTime = row.get("update_time");
        if (updateTime instanceof LocalDateTime ldt) vo.setUpdateTime(ldt);
        else if (updateTime instanceof java.sql.Timestamp ts) vo.setUpdateTime(ts.toLocalDateTime());
        return vo;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        int rows = jdbcTemplate.update(
            "UPDATE points_product SET status = ? WHERE id = ? AND status != ?", status, id, status);
        if (rows == 0) throw new BusinessException(ErrorCode.POINTS_PRODUCT_NOT_FOUND);
    }
}
