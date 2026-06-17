package com.salon.points.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.points.dto.PointsExchangeRecordReqDTO;
import com.salon.points.entity.PointsExchangeRecord;
import com.salon.points.mapper.PointsExchangeRecordMapper;
import com.salon.points.service.PointsExchangeRecordService;
import com.salon.points.vo.PointsExchangeRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsExchangeRecordServiceImpl extends ServiceImpl<PointsExchangeRecordMapper, PointsExchangeRecord>
    implements PointsExchangeRecordService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<PointsExchangeRecordVO> page(int page, int size, String memberPhone, Integer status,
                                              String startDate, String endDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, member_id, member_name, member_phone, product_id, product_name, " +
            "points_cost, quantity, status, remark, operator_id, exchanged_at, claimed_at, create_time " +
            "FROM points_exchange_record WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (memberPhone != null && !memberPhone.isEmpty()) {
            sql.append(" AND member_phone LIKE ?");
            params.add("%" + memberPhone + "%");
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND create_time >= ?");
            params.add(startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND create_time <= ?");
            params.add(endDate + " 23:59:59");
        }

        String countSql = "SELECT COUNT(*) FROM points_exchange_record WHERE 1=1" +
            (sql.indexOf("AND") >= 0 ? " " + sql.substring(sql.indexOf("AND")) : "");
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());

        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        List<PointsExchangeRecordVO> records = jdbcTemplate.query(sql.toString(),
            new BeanPropertyRowMapper<>(PointsExchangeRecordVO.class), params.toArray());

        Page<PointsExchangeRecordVO> result = new Page<>(page, size, total != null ? total : 0);
        result.setRecords(records != null ? records : new ArrayList<>());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PointsExchangeRecordVO exchange(PointsExchangeRecordReqDTO dto) {
        int quantity = dto.getQuantity() != null ? dto.getQuantity() : 1;

        // 1. Validate member
        Map<String, Object> memberRow;
        try {
            memberRow = jdbcTemplate.queryForMap(
                "SELECT id, name, phone, points FROM member WHERE id = ?", dto.getMemberId());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        int currentPoints = ((Number) memberRow.get("points")).intValue();
        String memberName = (String) memberRow.get("name");
        String memberPhone = (String) memberRow.get("phone");

        // 2. Validate product
        Map<String, Object> productRow;
        try {
            productRow = jdbcTemplate.queryForMap(
                "SELECT id, name, points_price, stock_qty, status FROM points_product WHERE id = ?", dto.getProductId());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.POINTS_PRODUCT_NOT_FOUND);
        }
        Integer productStatus = (Integer) productRow.get("status");
        if (!Integer.valueOf(1).equals(productStatus))
            throw new BusinessException(ErrorCode.POINTS_PRODUCT_OFF_SHELF);
        int pointsPrice = ((Number) productRow.get("points_price")).intValue();
        int stockQty = ((Number) productRow.get("stock_qty")).intValue();
        String productName = (String) productRow.get("name");

        // 3. Check stock
        if (stockQty < quantity)
            throw new BusinessException(ErrorCode.POINTS_PRODUCT_STOCK_INSUFFICIENT);

        // 4. Check points
        int totalPointsCost = pointsPrice * quantity;
        if (currentPoints < totalPointsCost)
            throw new BusinessException(ErrorCode.MEMBER_POINTS_INSUFFICIENT);

        // 5. Atomic stock deduction (A1 + H1)
        int stockRows = jdbcTemplate.update(
            "UPDATE points_product SET stock_qty = stock_qty - ?, exchanged_count = exchanged_count + ? WHERE id = ? AND stock_qty >= ?",
            quantity, quantity, dto.getProductId(), quantity);
        if (stockRows == 0)
            throw new BusinessException(ErrorCode.POINTS_PRODUCT_STOCK_INSUFFICIENT);

        // 6. Atomic points deduction (A1 + H1)
        int pointsRows = jdbcTemplate.update(
            "UPDATE member SET points = points - ? WHERE id = ? AND points >= ?",
            totalPointsCost, dto.getMemberId(), totalPointsCost);
        if (pointsRows == 0)
            throw new BusinessException(ErrorCode.MEMBER_POINTS_INSUFFICIENT);

        // 7. Insert exchange record
        int insertRows = jdbcTemplate.update(
            "INSERT INTO points_exchange_record (member_id, member_name, member_phone, product_id, product_name, points_cost, quantity, status, exchanged_at) VALUES (?, ?, ?, ?, ?, ?, ?, 0, NOW())",
            dto.getMemberId(), memberName, memberPhone, dto.getProductId(), productName, totalPointsCost, quantity);
        if (insertRows == 0) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "兑换记录创建失败");

        // 8. Query back the created record
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, member_id, member_name, member_phone, product_id, product_name, points_cost, quantity, status, exchanged_at FROM points_exchange_record WHERE member_id = ? ORDER BY id DESC LIMIT 1",
            dto.getMemberId());
        if (rows.isEmpty()) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "兑换记录查询失败");

        Map<String, Object> record = rows.get(0);
        PointsExchangeRecordVO vo = new PointsExchangeRecordVO();
        vo.setId(((Number) record.get("id")).longValue());
        vo.setMemberId(((Number) record.get("member_id")).longValue());
        vo.setMemberName((String) record.get("member_name"));
        vo.setMemberPhone((String) record.get("member_phone"));
        vo.setProductId(((Number) record.get("product_id")).longValue());
        vo.setProductName((String) record.get("product_name"));
        vo.setPointsCost(((Number) record.get("points_cost")).intValue());
        vo.setQuantity(((Number) record.get("quantity")).intValue());
        vo.setStatus(((Number) record.get("status")).intValue());
        Object exchangedAt = record.get("exchanged_at");
        if (exchangedAt instanceof java.time.LocalDateTime ldt) {
            vo.setExchangedAt(ldt);
        } else if (exchangedAt instanceof java.sql.Timestamp ts) {
            vo.setExchangedAt(ts.toLocalDateTime());
        }

        log.info("积分兑换: memberId={}, productId={}, quantity={}, pointsCost={}, remainingPoints={}",
            dto.getMemberId(), dto.getProductId(), quantity, totalPointsCost, currentPoints - totalPointsCost);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE points_exchange_record SET status = 1, claimed_at = NOW() WHERE id = ? AND status = 0", id);
        if (rows == 0) throw new BusinessException(ErrorCode.POINTS_EXCHANGE_STATUS_INVALID);
    }
}
