package com.salon.groupbuy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.groupbuy.dto.GroupBuyOrderReqDTO;
import com.salon.groupbuy.entity.GroupBuyOrder;
import com.salon.groupbuy.mapper.GroupBuyOrderMapper;
import com.salon.groupbuy.service.GroupBuyOrderService;
import com.salon.groupbuy.vo.GroupBuyOrderPageVO;
import com.salon.groupbuy.vo.GroupBuyOrderVO;
import com.salon.groupbuy.vo.GroupBuyParticipantVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyOrderServiceImpl extends ServiceImpl<GroupBuyOrderMapper, GroupBuyOrder>
    implements GroupBuyOrderService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<GroupBuyOrderPageVO> page(int page, int size, Long templateId, Integer status,
                                           String startDate, String endDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, template_id, order_no, leader_name, leader_phone, " +
            "group_price, group_size, current_size, expire_time, status, complete_time, create_time " +
            "FROM group_buy_order WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (templateId != null) {
            sql.append(" AND template_id = ?");
            params.add(templateId);
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

        String countSql = "SELECT COUNT(*) FROM group_buy_order WHERE 1=1" +
            (params.isEmpty() ? "" : sql.substring(sql.indexOf("AND") - 1));
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());

        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        List<GroupBuyOrderPageVO> records = jdbcTemplate.query(sql.toString(),
            new BeanPropertyRowMapper<>(GroupBuyOrderPageVO.class), params.toArray());

        Page<GroupBuyOrderPageVO> result = new Page<>(page, size, total != null ? total : 0);
        result.setRecords(records != null ? records : new ArrayList<>());
        return result;
    }

    @Override
    public GroupBuyOrderVO getDetail(Long id) {
        Map<String, Object> orderRow;
        try {
            orderRow = jdbcTemplate.queryForMap(
                "SELECT * FROM group_buy_order WHERE id = ?", id);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GROUP_BUY_ORDER_NOT_FOUND);
        }
        GroupBuyOrderVO vo = mapOrderRowToVO(orderRow);

        List<GroupBuyParticipantVO> participants = jdbcTemplate.query(
            "SELECT * FROM group_buy_participant WHERE order_id = ? ORDER BY is_leader DESC, join_time ASC",
            new BeanPropertyRowMapper<>(GroupBuyParticipantVO.class), id);
        vo.setParticipants(participants != null ? participants : new ArrayList<>());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupBuyOrderVO create(Long memberId, GroupBuyOrderReqDTO dto) {
        // 1. Query member
        Map<String, Object> memberRow;
        try {
            memberRow = jdbcTemplate.queryForMap(
                "SELECT id, name, phone, balance FROM member WHERE id = ?", memberId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        BigDecimal balance = (BigDecimal) memberRow.get("balance");
        String memberName = (String) memberRow.get("name");
        String memberPhone = (String) memberRow.get("phone");

        // 2. Query template
        Map<String, Object> templateRow;
        try {
            templateRow = jdbcTemplate.queryForMap(
                "SELECT * FROM group_buy_template WHERE id = ?", dto.getTemplateId());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_NOT_FOUND);
        }
        Integer templateStatus = (Integer) templateRow.get("status");
        if (!Integer.valueOf(1).equals(templateStatus))
            throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_NOT_ACTIVE);

        BigDecimal groupPrice = (BigDecimal) templateRow.get("group_price");
        int groupSize = ((Number) templateRow.get("group_size")).intValue();
        int expireHours = ((Number) templateRow.get("expire_hours")).intValue();
        // Check time range
        Object startTimeRaw = templateRow.get("start_time");
        Object endTimeRaw = templateRow.get("end_time");
        LocalDateTime now = LocalDateTime.now();
        if (startTimeRaw != null) {
            LocalDateTime startTime = toLocalDateTime(startTimeRaw);
            if (startTime != null && now.isBefore(startTime))
                throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_NOT_IN_TIME);
        }
        if (endTimeRaw != null) {
            LocalDateTime endTime = toLocalDateTime(endTimeRaw);
            if (endTime != null && now.isAfter(endTime))
                throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_NOT_IN_TIME);
        }

        // 3. Check balance
        if (balance.compareTo(groupPrice) < 0)
            throw new BusinessException(ErrorCode.GROUP_BUY_MEMBER_BALANCE_INSUFFICIENT);

        // 4. Atomic: deduct template stock
        int stockRows = jdbcTemplate.update(
            "UPDATE group_buy_template SET issued_qty = issued_qty + 1 WHERE id = ? AND (total_qty = 0 OR issued_qty < total_qty)",
            dto.getTemplateId());
        if (stockRows == 0)
            throw new BusinessException(ErrorCode.GROUP_BUY_TEMPLATE_EXHAUSTED);

        // 5. Insert balance record BEFORE balance change (F2 rule + R4: rows check)
        int recordRows = jdbcTemplate.update(
            "INSERT INTO recharge_record (member_id, amount, pay_method, remark) VALUES (?, ?, 5, ?)",
            memberId, groupPrice.negate(), "拼团开团");
        if (recordRows == 0)
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "流水记录插入失败");

        // 6. Atomic: deduct balance
        int balanceRows = jdbcTemplate.update(
            "UPDATE member SET balance = balance - ? WHERE id = ? AND balance >= ?",
            groupPrice, memberId, groupPrice);
        if (balanceRows == 0)
            throw new BusinessException(ErrorCode.GROUP_BUY_MEMBER_BALANCE_INSUFFICIENT);

        // 7. Insert order
        String orderNo = "GB" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        LocalDateTime expireTime = now.plusHours(expireHours);
        BigDecimal originalPrice = (BigDecimal) templateRow.get("original_price");
        jdbcTemplate.update(
            "INSERT INTO group_buy_order (template_id, order_no, leader_id, leader_name, leader_phone, group_price, original_price, group_size, current_size, expire_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, 1)",
            dto.getTemplateId(), orderNo, memberId, memberName, memberPhone,
            groupPrice, originalPrice, groupSize, expireTime);

        // 8. Insert leader participant
        jdbcTemplate.update(
            "INSERT INTO group_buy_participant (order_id, member_id, member_name, member_phone, join_price, status, is_leader, join_time) VALUES ((SELECT id FROM group_buy_order WHERE order_no = ?), ?, ?, ?, ?, 1, 1, NOW())",
            orderNo, memberId, memberName, memberPhone, groupPrice);

        log.info("拼团开团: memberId={}, templateId={}, orderNo={}, price={}, balance={}",
            memberId, dto.getTemplateId(), orderNo, groupPrice, balance.subtract(groupPrice));

        // 9. Query back
        Map<String, Object> created = jdbcTemplate.queryForMap(
            "SELECT * FROM group_buy_order WHERE order_no = ?", orderNo);
        return getDetail(((Number) created.get("id")).longValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupBuyOrderVO join(Long orderId, Long memberId) {
        // 1. Query member
        Map<String, Object> memberRow;
        try {
            memberRow = jdbcTemplate.queryForMap(
                "SELECT id, name, phone, balance FROM member WHERE id = ?", memberId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        BigDecimal balance = (BigDecimal) memberRow.get("balance");
        String memberName = (String) memberRow.get("name");
        String memberPhone = (String) memberRow.get("phone");

        // 2. Query order
        Map<String, Object> orderRow;
        try {
            orderRow = jdbcTemplate.queryForMap(
                "SELECT * FROM group_buy_order WHERE id = ?", orderId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GROUP_BUY_ORDER_NOT_FOUND);
        }
        Integer orderStatus = (Integer) orderRow.get("status");
        int currentSize = ((Number) orderRow.get("current_size")).intValue();
        int groupSize = ((Number) orderRow.get("group_size")).intValue();
        BigDecimal groupPrice = (BigDecimal) orderRow.get("group_price");
        Object expireTimeRaw = orderRow.get("expire_time");

        // Validate status
        if (!Integer.valueOf(1).equals(orderStatus))
            throw new BusinessException(ErrorCode.GROUP_BUY_ORDER_CLOSED);

        // Validate expire
        LocalDateTime expireTime = toLocalDateTime(expireTimeRaw);
        if (expireTime != null && LocalDateTime.now().isAfter(expireTime))
            throw new BusinessException(ErrorCode.GROUP_BUY_ORDER_EXPIRED);

        // Validate not full
        if (currentSize >= groupSize)
            throw new BusinessException(ErrorCode.GROUP_BUY_ORDER_FULL);

        // 3. Check not already joined
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM group_buy_participant WHERE order_id = ? AND member_id = ?",
            Integer.class, orderId, memberId);
        if (count != null && count > 0)
            throw new BusinessException(ErrorCode.GROUP_BUY_ALREADY_JOINED);

        // 4. Check balance
        if (balance.compareTo(groupPrice) < 0)
            throw new BusinessException(ErrorCode.GROUP_BUY_MEMBER_BALANCE_INSUFFICIENT);

        // 5. Insert balance record BEFORE balance change (F2 rule + R4: rows check)
        int recordRows = jdbcTemplate.update(
            "INSERT INTO recharge_record (member_id, amount, pay_method, remark) VALUES (?, ?, 5, ?)",
            memberId, groupPrice.negate(), "拼团参团");
        if (recordRows == 0)
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "流水记录插入失败");

        // 6. Atomic: deduct balance
        int balanceRows = jdbcTemplate.update(
            "UPDATE member SET balance = balance - ? WHERE id = ? AND balance >= ?",
            groupPrice, memberId, groupPrice);
        if (balanceRows == 0)
            throw new BusinessException(ErrorCode.GROUP_BUY_MEMBER_BALANCE_INSUFFICIENT);

        // 7. Atomic: increment group size + detect completion
        int newSize = currentSize + 1;
        boolean willComplete = newSize >= groupSize;
        int orderRows = jdbcTemplate.update(
            willComplete
                ? "UPDATE group_buy_order SET current_size = current_size + 1, status = 2, complete_time = NOW() WHERE id = ? AND current_size < group_size AND status = 1"
                : "UPDATE group_buy_order SET current_size = current_size + 1 WHERE id = ? AND current_size < group_size AND status = 1",
            orderId);
        if (orderRows == 0)
            throw new BusinessException(ErrorCode.GROUP_BUY_ORDER_FULL);

        // 8. Insert participant
        int participantStatus = willComplete ? 2 : 1;
        jdbcTemplate.update(
            "INSERT INTO group_buy_participant (order_id, member_id, member_name, member_phone, join_price, status, is_leader, join_time) VALUES (?, ?, ?, ?, ?, ?, 0, NOW())",
            orderId, memberId, memberName, memberPhone, groupPrice, participantStatus);

        // 9. If completed, update leader and all existing participants to status=2
        if (willComplete) {
            int participantRows = jdbcTemplate.update(
                "UPDATE group_buy_participant SET status = 2, complete_time = NOW() WHERE order_id = ? AND status = 1",
                orderId);
            if (participantRows == 0) {
                log.warn("拼团成团批量更新参与者状态异常: orderId={}, 预期至少1条记录", orderId);
            }
        }

        log.info("拼团参团: memberId={}, orderId={}, price={}, newSize={}, completed={}, balance={}",
            memberId, orderId, groupPrice, newSize, willComplete, balance.subtract(groupPrice));

        // 10. Return full detail
        return getDetail(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redeem(Long participantId) {
        int rows = jdbcTemplate.update(
            "UPDATE group_buy_participant SET status = 3 WHERE id = ? AND status = 2", participantId);
        if (rows == 0)
            throw new BusinessException(ErrorCode.GROUP_BUY_PARTICIPANT_STATUS_INVALID);
    }

    @Override
    public Page<GroupBuyOrderPageVO> myOrders(Long memberId, int page, int size) {
        String sql = "SELECT DISTINCT o.id, o.template_id, o.order_no, o.leader_name, o.leader_phone, " +
            "o.group_price, o.group_size, o.current_size, o.expire_time, o.status, o.complete_time, o.create_time " +
            "FROM group_buy_order o INNER JOIN group_buy_participant p ON o.id = p.order_id " +
            "WHERE p.member_id = ? ORDER BY o.create_time DESC LIMIT ? OFFSET ?";

        String countSql = "SELECT COUNT(DISTINCT o.id) FROM group_buy_order o " +
            "INNER JOIN group_buy_participant p ON o.id = p.order_id WHERE p.member_id = ?";

        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, memberId);
        List<GroupBuyOrderPageVO> records = jdbcTemplate.query(sql,
            new BeanPropertyRowMapper<>(GroupBuyOrderPageVO.class),
            memberId, size, (page - 1) * size);

        Page<GroupBuyOrderPageVO> result = new Page<>(page, size, total != null ? total : 0);
        result.setRecords(records != null ? records : new ArrayList<>());
        return result;
    }

    // -- helpers --

    private GroupBuyOrderVO mapOrderRowToVO(Map<String, Object> row) {
        GroupBuyOrderVO vo = new GroupBuyOrderVO();
        vo.setId(((Number) row.get("id")).longValue());
        vo.setTemplateId(((Number) row.get("template_id")).longValue());
        vo.setOrderNo((String) row.get("order_no"));
        vo.setLeaderId(((Number) row.get("leader_id")).longValue());
        vo.setLeaderName((String) row.get("leader_name"));
        vo.setLeaderPhone((String) row.get("leader_phone"));
        vo.setGroupPrice((BigDecimal) row.get("group_price"));
        vo.setOriginalPrice((BigDecimal) row.get("original_price"));
        vo.setGroupSize(((Number) row.get("group_size")).intValue());
        vo.setCurrentSize(((Number) row.get("current_size")).intValue());
        vo.setExpireTime(toLocalDateTime(row.get("expire_time")));
        vo.setStatus(((Number) row.get("status")).intValue());
        vo.setCompleteTime(toLocalDateTime(row.get("complete_time")));
        vo.setCreateTime(toLocalDateTime(row.get("create_time")));
        return vo;
    }

    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDateTime ldt) return ldt;
        if (obj instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}
