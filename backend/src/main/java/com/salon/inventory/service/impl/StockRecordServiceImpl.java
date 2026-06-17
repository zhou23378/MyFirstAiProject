package com.salon.inventory.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.inventory.dto.StockRecordReqDTO;
import com.salon.inventory.entity.Product;
import com.salon.inventory.entity.StockRecord;
import com.salon.inventory.mapper.StockRecordMapper;
import com.salon.inventory.service.ProductService;
import com.salon.inventory.service.StockRecordService;
import com.salon.inventory.vo.StockRecordPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockRecordServiceImpl extends ServiceImpl<StockRecordMapper, StockRecord> implements StockRecordService {

    private final JdbcTemplate jdbcTemplate;
    private final ProductService productService;

    @Override
    public Page<StockRecordPageVO> pageWithNames(int page, int pageSize, Integer type) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (type != null) {
            where.append(" AND sr.type = ?");
            params.add(type);
        }

        String countSql = "SELECT COUNT(*) FROM stock_record sr WHERE 1=1" + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);
        String dataSql = "SELECT sr.id, sr.product_id AS productId, sr.supplier_id AS supplierId, " +
                "sr.type, sr.qty, sr.price, sr.total_amount AS totalAmount, " +
                "sr.operator, sr.remark, sr.create_time AS createTime, sr.update_time AS updateTime, " +
                "p.name AS productName, sup.name AS supplierName " +
                "FROM stock_record sr " +
                "LEFT JOIN product p ON sr.product_id = p.id " +
                "LEFT JOIN supplier sup ON sr.supplier_id = sup.id " +
                "WHERE 1=1" + where +
                " ORDER BY sr.create_time DESC LIMIT ?, ?";
        List<StockRecordPageVO> list = jdbcTemplate.query(dataSql, new BeanPropertyRowMapper<>(StockRecordPageVO.class), params.toArray());

        Page<StockRecordPageVO> result = new Page<>(page, pageSize);
        result.setTotal(total != null ? total : 0);
        result.setRecords(list);
        return result;
    }

    @Override
    @Transactional
    public StockRecord createRecord(StockRecordReqDTO dto, String operator) {
        Product product = productService.getById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        StockRecord record = new StockRecord();
        record.setProductId(dto.getProductId());
        record.setType(dto.getType());
        record.setQty(dto.getQty());
        record.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        record.setTotalAmount(dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO);
        record.setSupplierId(dto.getSupplierId());
        record.setOperator(operator);
        record.setRemark(dto.getRemark());

        // 更新库存：入库 + 增，出库/退货 - 减（原子操作，避免并发写丢失）
        int delta = dto.getQty();
        if (dto.getType() == 2 || dto.getType() == 3) {
            delta = -delta;
        }
        int rows = jdbcTemplate.update(
            "UPDATE product SET stock_qty = stock_qty + ? WHERE id = ? AND stock_qty + ? >= 0",
            delta, dto.getProductId(), delta);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT, "库存不足，当前库存：" + product.getStockQty());
        }

        save(record);
        log.info("库存变更: productId={}, type={}, qty={}, operator={}",
            dto.getProductId(), dto.getType(), dto.getQty(), operator);
        return record;
    }
}
