package com.salon.commission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.commission.dto.CommissionSettlementReqDTO;
import com.salon.commission.entity.CommissionSettlement;
import com.salon.commission.mapper.CommissionSettlementMapper;
import com.salon.commission.service.CommissionSettlementService;
import com.salon.commission.vo.CommissionSettlementItemVO;
import com.salon.commission.vo.CommissionSettlementPageVO;
import com.salon.commission.vo.CommissionSettlementVO;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionSettlementServiceImpl extends ServiceImpl<CommissionSettlementMapper, CommissionSettlement>
    implements CommissionSettlementService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<CommissionSettlementPageVO> page(int page, int size, Long employeeId, Integer status,
                                                  String periodStart, String periodEnd) {
        LambdaQueryWrapper<CommissionSettlement> w = new LambdaQueryWrapper<>();
        if (employeeId != null) w.eq(CommissionSettlement::getEmployeeId, employeeId);
        if (status != null) w.eq(CommissionSettlement::getStatus, status);
        if (periodStart != null) w.ge(CommissionSettlement::getPeriodStart, periodStart);
        if (periodEnd != null) w.le(CommissionSettlement::getPeriodEnd, periodEnd);
        w.orderByDesc(CommissionSettlement::getCreateTime);

        Page<CommissionSettlement> entityPage = baseMapper.selectPage(
            new Page<>(page, size), w);

        Page<CommissionSettlementPageVO> result = new Page<>(page, size, entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(e -> {
            CommissionSettlementPageVO vo = new CommissionSettlementPageVO();
            vo.setId(e.getId());
            vo.setEmployeeId(e.getEmployeeId());
            vo.setEmployeeName(e.getEmployeeName());
            vo.setPeriodStart(e.getPeriodStart());
            vo.setPeriodEnd(e.getPeriodEnd());
            vo.setOrderCount(e.getOrderCount());
            vo.setTotalCommission(e.getTotalCommission());
            vo.setStatus(e.getStatus());
            vo.setCreateTime(e.getCreateTime());
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    @Override
    public CommissionSettlementVO getDetail(Long id) {
        CommissionSettlement entity = getById(id);
        if (entity == null) throw new BusinessException(ErrorCode.COMMISSION_SETTLEMENT_NOT_FOUND);

        CommissionSettlementVO vo = new CommissionSettlementVO();
        vo.setId(entity.getId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setPeriodStart(entity.getPeriodStart());
        vo.setPeriodEnd(entity.getPeriodEnd());
        vo.setOrderCount(entity.getOrderCount());
        vo.setTotalCommission(entity.getTotalCommission());
        vo.setStatus(entity.getStatus());
        vo.setConfirmedAt(entity.getConfirmedAt());
        vo.setPaidAt(entity.getPaidAt());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());

        // Query order items in the settlement period for this employee
        String itemSql = "SELECT o.id as orderId, CAST(o.id AS CHAR) as orderNo, " +
            "GROUP_CONCAT(oi.item_name ORDER BY oi.id SEPARATOR ', ') as serviceNames, " +
            "o.total_amount as orderAmount, o.commission_amount as commissionAmount, " +
            "o.create_time as orderTime " +
            "FROM consumption_order o " +
            "JOIN consumption_order_item oi ON oi.order_id = o.id " +
            "WHERE o.employee_id = ? AND o.status = 1 " +
            "AND o.create_time >= ? AND o.create_time < ? " +
            "GROUP BY o.id, o.total_amount, o.commission_amount, o.create_time " +
            "ORDER BY o.create_time DESC";
        LocalDateTime periodStart = entity.getPeriodStart().atStartOfDay();
        LocalDateTime periodEnd = entity.getPeriodEnd().plusDays(1).atStartOfDay();
        List<CommissionSettlementItemVO> items = jdbcTemplate.query(itemSql,
            new BeanPropertyRowMapper<>(CommissionSettlementItemVO.class),
            entity.getEmployeeId(), periodStart, periodEnd);
        vo.setItems(items != null ? items : new ArrayList<>());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CommissionSettlementVO> create(CommissionSettlementReqDTO dto) {
        LocalDate start = dto.getPeriodStart();
        LocalDate end = dto.getPeriodEnd();
        if (start.isAfter(end)) throw new BusinessException(ErrorCode.BAD_REQUEST, "起始日期不能晚于截止日期");

        // Check duplicate
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM commission_settlement WHERE period_start = ? AND period_end = ?",
            Integer.class, start, end);
        if (count != null && count > 0)
            throw new BusinessException(ErrorCode.COMMISSION_SETTLEMENT_PERIOD_EXISTS);

        // Aggregate consumption orders by employee
        String aggSql = "SELECT o.employee_id as employeeId, e.name as employeeName, " +
            "COUNT(DISTINCT o.id) as orderCount, " +
            "COALESCE(SUM(o.commission_amount), 0) as totalCommission " +
            "FROM consumption_order o " +
            "JOIN employee e ON e.id = o.employee_id " +
            "WHERE o.employee_id IS NOT NULL AND o.status = 1 " +
            "AND o.create_time >= ? AND o.create_time < ? " +
            "GROUP BY o.employee_id, e.name " +
            "ORDER BY totalCommission DESC";
        LocalDateTime periodStart = start.atStartOfDay();
        LocalDateTime periodEnd = end.plusDays(1).atStartOfDay();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(aggSql, periodStart, periodEnd);
        if (rows.isEmpty()) throw new BusinessException(ErrorCode.COMMISSION_SETTLEMENT_NO_DATA);

        List<CommissionSettlement> entities = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            CommissionSettlement entity = new CommissionSettlement();
            entity.setEmployeeId(((Number) row.get("employeeId")).longValue());
            entity.setEmployeeName((String) row.get("employeeName"));
            entity.setPeriodStart(start);
            entity.setPeriodEnd(end);
            entity.setOrderCount(((Number) row.get("orderCount")).intValue());
            BigDecimal total = new BigDecimal(row.get("totalCommission").toString())
                .setScale(2, RoundingMode.HALF_UP);
            entity.setTotalCommission(total);
            entity.setStatus(0);
            entities.add(entity);
        }

        // Batch insert
        String insertSql = "INSERT INTO commission_settlement " +
            "(employee_id, employee_name, period_start, period_end, order_count, total_commission, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, 0)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (CommissionSettlement entity : entities) {
            batchArgs.add(new Object[]{entity.getEmployeeId(), entity.getEmployeeName(),
                entity.getPeriodStart(), entity.getPeriodEnd(), entity.getOrderCount(), entity.getTotalCommission()});
        }
        int[] batchResults = jdbcTemplate.batchUpdate(insertSql, batchArgs);
        int successCount = 0;
        for (int r : batchResults) {
            if (r == 1 || r == java.sql.Statement.SUCCESS_NO_INFO) successCount++;
        }
        if (successCount != entities.size())
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "提成结算批量写入失败");

        // Re-read inserted entities via jdbcTemplate to get generated IDs (A4: no MyBatis read after jdbcTemplate write)
        List<Map<String, Object>> saved = jdbcTemplate.queryForList(
            "SELECT id, employee_id, employee_name, period_start, period_end, order_count, total_commission, status, create_time " +
            "FROM commission_settlement WHERE period_start = ? AND period_end = ? ORDER BY total_commission DESC",
            start, end);

        List<CommissionSettlementVO> result = new ArrayList<>();
        for (Map<String, Object> row : saved) {
            CommissionSettlementVO vo = new CommissionSettlementVO();
            vo.setId(((Number) row.get("id")).longValue());
            vo.setEmployeeId(((Number) row.get("employee_id")).longValue());
            vo.setEmployeeName((String) row.get("employee_name"));
            vo.setPeriodStart(((java.sql.Date) row.get("period_start")).toLocalDate());
            vo.setPeriodEnd(((java.sql.Date) row.get("period_end")).toLocalDate());
            vo.setOrderCount(((Number) row.get("order_count")).intValue());
            vo.setTotalCommission(new BigDecimal(row.get("total_commission").toString()).setScale(2, RoundingMode.HALF_UP));
            vo.setStatus(((Number) row.get("status")).intValue());
            Object createTime = row.get("create_time");
            if (createTime instanceof LocalDateTime ldt) vo.setCreateTime(ldt);
            else if (createTime instanceof java.sql.Timestamp ts) vo.setCreateTime(ts.toLocalDateTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE commission_settlement SET status = 1, confirmed_at = NOW() WHERE id = ? AND status = 0", id);
        if (rows == 0) throw new BusinessException(ErrorCode.COMMISSION_SETTLEMENT_STATUS_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE commission_settlement SET status = 2, paid_at = NOW() WHERE id = ? AND status = 1", id);
        if (rows == 0) throw new BusinessException(ErrorCode.COMMISSION_SETTLEMENT_STATUS_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        int rows = jdbcTemplate.update(
            "DELETE FROM commission_settlement WHERE id = ? AND status = 0", id);
        if (rows == 0) throw new BusinessException(ErrorCode.COMMISSION_SETTLEMENT_STATUS_INVALID);
    }
}
