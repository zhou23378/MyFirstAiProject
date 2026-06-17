package com.salon.consumption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.consumption.dto.CreateOrderDTO;
import com.salon.consumption.dto.ResumeOrderReqDTO;
import com.salon.consumption.dto.SuspendOrderReqDTO;
import com.salon.consumption.entity.ConsumptionOrder;
import com.salon.consumption.entity.ConsumptionOrderItem;
import com.salon.consumption.entity.PaymentDetail;
import com.salon.common.enums.PayMethodEnum;
import com.salon.consumption.mapper.ConsumptionOrderItemMapper;
import com.salon.consumption.mapper.ConsumptionOrderMapper;
import com.salon.consumption.service.ConsumptionOrderService;
import com.salon.consumption.vo.SuspendedOrderVO;
import com.salon.payment.service.PaymentDetailService;
import com.salon.coupon.mapper.CouponMapper;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.memberlevel.entity.MemberLevel;
import com.salon.memberlevel.mapper.MemberLevelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消费订单 Service 实现
 * <p>
 * 实现创建消费订单的业务逻辑，包含等级折扣计算和自动升级
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumptionOrderServiceImpl extends ServiceImpl<ConsumptionOrderMapper, ConsumptionOrder> implements ConsumptionOrderService {

    private final ConsumptionOrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final PaymentDetailService paymentDetailService;
    private final CouponMapper couponMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 创建消费订单
     * <p>
     * 1. 校验会员是否存在
     * 2. 根据会员等级计算折扣价
     * 3. 创建订单主表记录
     * 4. 创建订单明细记录
     * 5. 更新会员余额和积分
     * 6. 自动升级会员等级
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumptionOrder createOrder(CreateOrderDTO dto) {
        // 1. 校验会员
        Member member = memberMapper.selectById(dto.getMemberId());
        if (member == null) {
            throw BusinessException.notFound("会员不存在，id=" + dto.getMemberId());
        }

        // 2. 获取会员等级折扣率
        BigDecimal discountRate = BigDecimal.ONE;
        if (member.getLevel() != null && member.getLevel() > 0) {
            MemberLevel memberLevel = memberLevelMapper.selectById(member.getLevel().longValue());
            if (memberLevel != null && memberLevel.getDiscountRate() != null) {
                discountRate = memberLevel.getDiscountRate();
            }
        }

        // 3. 计算原总金额
        BigDecimal originalTotal = dto.getItems().stream()
                .map(item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 应用等级折扣
        BigDecimal totalAmount = originalTotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

        if (discountRate.compareTo(BigDecimal.ONE) < 0) {
            log.info("会员等级折扣: memberId={}, level={}, discountRate={}, original={}, actual={}",
                    dto.getMemberId(), member.getLevel(), discountRate, originalTotal, totalAmount);
        }

        // 5. 创建订单
        ConsumptionOrder order = new ConsumptionOrder();
        order.setMemberId(dto.getMemberId());
        order.setTotalAmount(totalAmount);
        order.setPayMethod(dto.getPayMethod());
        order.setPayAmount(dto.getPayAmount());
        order.setBalanceUsed(dto.getBalanceUsed() != null ? dto.getBalanceUsed() :
                (Integer.valueOf(2).equals(dto.getPayMethod()) ? totalAmount : null));
        order.setPointsEarned(totalAmount.setScale(0, RoundingMode.HALF_UP).intValue());
        order.setStatus(1);
        order.setPayRemark(dto.getPayRemark());
        order.setEmployeeId(dto.getEmployeeId());

        // 6. 计算提成（单条 IN 查询，避免 N+1）
        BigDecimal commissionAmount = BigDecimal.ZERO;
        if (dto.getEmployeeId() != null) {
            List<Long> itemIds = dto.getItems().stream()
                    .map(CreateOrderDTO.OrderItemDTO::getItemId).distinct().collect(Collectors.toList());
            if (!itemIds.isEmpty()) {
                String placeholders = itemIds.stream().map(id -> "?").collect(Collectors.joining(","));
                List<Map<String, Object>> commRows = jdbcTemplate.queryForList(
                    "SELECT id, commission_type, commission_value FROM service_item WHERE id IN (" + placeholders + ")",
                    itemIds.toArray());
                Map<Long, Map<String, Object>> commMap = commRows.stream()
                    .collect(Collectors.toMap(r -> ((Number) r.get("id")).longValue(), r -> r));
                for (CreateOrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
                    Map<String, Object> row = commMap.get(itemDTO.getItemId());
                    if (row != null) {
                        Number ct = (Number) row.get("commission_type");
                        Number cv = (Number) row.get("commission_value");
                        if (ct != null && cv != null && ct.intValue() > 0) {
                            if (ct.intValue() == 1) {
                                commissionAmount = commissionAmount.add(
                                    new BigDecimal(cv.toString()).multiply(BigDecimal.valueOf(itemDTO.getQuantity()))
                                        .setScale(2, RoundingMode.HALF_UP));
                            } else if (ct.intValue() == 2) {
                                commissionAmount = commissionAmount.add(
                                    itemDTO.getItemPrice().multiply(new BigDecimal(cv.toString()))
                                        .multiply(BigDecimal.valueOf(itemDTO.getQuantity()))
                                        .setScale(2, RoundingMode.HALF_UP));
                            }
                        }
                    }
                }
            }
        }
        order.setCommissionAmount(commissionAmount);
        baseMapper.insert(order);

        // 7. 批量插入明细
        List<ConsumptionOrderItem> orderItems = new ArrayList<>();
        for (CreateOrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            ConsumptionOrderItem item = new ConsumptionOrderItem();
            item.setOrderId(order.getId());
            item.setItemId(itemDTO.getItemId());
            item.setItemName(itemDTO.getItemName());
            item.setItemPrice(itemDTO.getItemPrice());
            item.setQuantity(itemDTO.getQuantity());
            orderItems.add(item);
        }
        List<Object[]> itemBatchArgs = new ArrayList<>();
        for (ConsumptionOrderItem oi : orderItems) {
            itemBatchArgs.add(new Object[]{oi.getOrderId(), oi.getItemId(), oi.getItemName(), oi.getItemPrice(), oi.getQuantity()});
        }
        int[] itemResults = jdbcTemplate.batchUpdate(
            "INSERT INTO consumption_order_item (order_id, item_id, item_name, item_price, quantity) VALUES (?, ?, ?, ?, ?)",
            itemBatchArgs);
        int itemSuccessCount = 0;
        for (int r : itemResults) { if (r == 1 || r == java.sql.Statement.SUCCESS_NO_INFO) itemSuccessCount++; }
        if (itemSuccessCount != orderItems.size())
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "订单明细批量写入失败");

        // 8. 组合支付：校验金额 + 插入支付明细
        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            BigDecimal totalPayments = dto.getPayments().stream()
                    .map(CreateOrderDTO.PaymentDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            if (totalPayments.compareTo(totalAmount) != 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                    String.format("支付金额合计(%.2f)与订单金额(%.2f)不一致", totalPayments, totalAmount));
            }
            order.setPayMethod(8);
            BigDecimal balanceDeduct = BigDecimal.ZERO;
            List<PaymentDetail> paymentDetails = new ArrayList<>();
            for (CreateOrderDTO.PaymentDTO p : dto.getPayments()) {
                PaymentDetail detail = new PaymentDetail();
                detail.setOrderId(order.getId());
                detail.setPayMethod(p.getPayMethod());
                detail.setAmount(p.getAmount());
                detail.setPayChannel(PayMethodEnum.of(p.getPayMethod()).name());
                paymentDetails.add(detail);
                if (Integer.valueOf(2).equals(p.getPayMethod())) {
                    balanceDeduct = balanceDeduct.add(p.getAmount());
                }
            }
            paymentDetailService.batchInsert(paymentDetails);
            order.setPayAmount(totalPayments.subtract(balanceDeduct).setScale(2, RoundingMode.HALF_UP));
            order.setBalanceUsed(balanceDeduct.setScale(2, RoundingMode.HALF_UP));
            baseMapper.updateById(order);
        }

        // 9. 优惠券核销（同一事务内）
        if (dto.getCouponCode() != null && !dto.getCouponCode().isBlank()) {
            List<Map<String, Object>> couponRows = jdbcTemplate.queryForList(
                "SELECT id, status, discount_value, condition_amount, expire_time FROM coupon WHERE code = ?",
                dto.getCouponCode());
            if (couponRows.isEmpty()) {
                throw new BusinessException(ErrorCode.COUPON_NOT_FOUND);
            }
            Map<String, Object> c = couponRows.get(0);
            int cStatus = (int) c.get("status");
            if (cStatus == 2) throw new BusinessException(ErrorCode.COUPON_USED);
            if (cStatus == 3) throw new BusinessException(ErrorCode.COUPON_EXPIRED);
            Object expireRaw = c.get("expire_time");
            LocalDateTime expireTime = expireRaw instanceof LocalDateTime ? (LocalDateTime) expireRaw
                : expireRaw instanceof java.sql.Timestamp ? ((java.sql.Timestamp) expireRaw).toLocalDateTime()
                : null;
            if (expireTime != null && expireTime.isBefore(LocalDateTime.now(ZONE))) {
                throw new BusinessException(ErrorCode.COUPON_EXPIRED);
            }
            BigDecimal conditionAmount = (BigDecimal) c.get("condition_amount");
            if (conditionAmount != null && conditionAmount.compareTo(BigDecimal.ZERO) > 0
                    && totalAmount.compareTo(conditionAmount) < 0) {
                throw new BusinessException(ErrorCode.COUPON_THRESHOLD_NOT_MET,
                    String.format("未满足门槛，需满 ¥%.2f", conditionAmount));
            }
            BigDecimal discountValue = (BigDecimal) c.get("discount_value");
            BigDecimal discount = discountValue != null ? discountValue : BigDecimal.ZERO;
            if (discount.compareTo(totalAmount) > 0) discount = totalAmount;
            // 原子更新：只有 status = 1 时才更新成功，防并发
            int updateRows = jdbcTemplate.update(
                "UPDATE coupon SET status = 2, use_time = ?, order_id = ? WHERE id = ? AND status = 1",
                LocalDateTime.now(ZONE), order.getId(), ((Number) c.get("id")).longValue());
            if (updateRows == 0) throw new BusinessException(ErrorCode.COUPON_USED, "优惠券已使用或已失效");
            totalAmount = totalAmount.subtract(discount).max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
            order.setCouponId(((Number) c.get("id")).longValue());
            order.setCouponDiscount(discount);
            order.setTotalAmount(totalAmount);
            baseMapper.updateById(order);
        }

        // 10. 原子化更新会员余额、积分、消费总额、访次、最后消费日期（防并发读写覆盖）
        BigDecimal balanceToDeduct = order.getBalanceUsed() != null ? order.getBalanceUsed() : BigDecimal.ZERO;
        int memberRows;
        if (balanceToDeduct.compareTo(BigDecimal.ZERO) > 0) {
            memberRows = jdbcTemplate.update(
                "UPDATE member SET balance = balance - ?, points = points + ?, "
                + "total_spent = total_spent + ?, visit_count = visit_count + 1, last_consume_date = ? "
                + "WHERE id = ? AND balance >= ?",
                balanceToDeduct, order.getPointsEarned(), order.getTotalAmount(),
                LocalDate.now(ZONE), member.getId(), balanceToDeduct);
            if (memberRows == 0) throw new BusinessException(ErrorCode.MEMBER_BALANCE_INSUFFICIENT);
        } else {
            memberRows = jdbcTemplate.update(
                "UPDATE member SET points = points + ?, total_spent = total_spent + ?, "
                + "visit_count = visit_count + 1, last_consume_date = ? WHERE id = ?",
                order.getPointsEarned(), order.getTotalAmount(), LocalDate.now(ZONE), member.getId());
            if (memberRows == 0) throw new BusinessException(ErrorCode.BAD_REQUEST, "更新会员失败");
        }

        // 11. 等级自动升级评估（手动同步内存，避免 A4 MyBatis 缓存返回旧数据）
        member.setBalance(member.getBalance().subtract(balanceToDeduct));
        member.setPoints(member.getPoints() + order.getPointsEarned());
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .le(MemberLevel::getPointsRequired, member.getPoints())
                        .orderByDesc(MemberLevel::getPointsRequired));
        if (!levels.isEmpty()) {
            int newLevel = levels.get(0).getId().intValue();
            if (member.getLevel() == null || newLevel != member.getLevel()) {
                int levelRows = jdbcTemplate.update("UPDATE member SET level = ? WHERE id = ?", newLevel, member.getId());
                if (levelRows == 0) {
                    throw new BusinessException(ErrorCode.INTERNAL_ERROR, "会员升级失败");
                }
                log.info("会员升级: id={}, fromLevel={}, toLevel={}, points={}",
                    member.getId(), member.getLevel(), newLevel, member.getPoints());
            }
        }

        log.info("创建订单成功: orderId={}, memberId={}, originalAmount={}, actualAmount={}, discountRate={}, couponDiscount={}",
                order.getId(), dto.getMemberId(), originalTotal, order.getTotalAmount(), discountRate, order.getCouponDiscount());

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumptionOrder refundOrder(Long id) {
        ConsumptionOrder order = baseMapper.selectById(id);
        if (order == null) throw BusinessException.notFound("订单不存在");

        // 原子标记退款状态，防并发双重退款
        int rows = jdbcTemplate.update(
            "UPDATE consumption_order SET status = 2 WHERE id = ? AND status = 1", id);
        if (rows == 0) throw new BusinessException(ErrorCode.ORDER_REFUNDED);

        // 恢复优惠券（先于 member 更新，与 createOrder 保持一致的锁顺序 coupon→member 防死锁）
        if (order.getCouponId() != null) {
            int cpnRows = jdbcTemplate.update(
                "UPDATE coupon SET status = 1, use_time = NULL, order_id = NULL WHERE id = ?",
                order.getCouponId());
            if (cpnRows == 0) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退款异常：优惠券恢复失败");
        }

        // 退回余额（原子化，检查 affected rows 防静默失败）
        if (order.getBalanceUsed() != null && order.getBalanceUsed().compareTo(BigDecimal.ZERO) > 0) {
            int balRows = jdbcTemplate.update(
                "UPDATE member SET balance = balance + ? WHERE id = ?",
                order.getBalanceUsed(), order.getMemberId());
            if (balRows == 0) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退款异常：会员不存在，余额退回失败");
        }

        // 退回积分（原子化）
        if (order.getPointsEarned() != null && order.getPointsEarned() > 0) {
            int ptsRows = jdbcTemplate.update(
                "UPDATE member SET points = GREATEST(0, points - ?) WHERE id = ?",
                order.getPointsEarned(), order.getMemberId());
            if (ptsRows == 0) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退款异常：会员不存在，积分退回失败");
        }

        // 等级降级评估（手动同步，避免 A4 MyBatis 缓存返回旧数据）
        Long memberId = order.getMemberId();
        BigDecimal refundedBalance = order.getBalanceUsed() != null ? order.getBalanceUsed() : BigDecimal.ZERO;
        int refundedPoints = order.getPointsEarned() != null ? order.getPointsEarned() : 0;
        // 从 DB 读取最新的 points 和 level，避免 jdbcTemplate 写后 MyBatis 缓存返回旧值
        Map<String, Object> memberRow = jdbcTemplate.queryForMap(
            "SELECT points, level FROM member WHERE id = ?", memberId);
        Integer currentPoints = ((Number) memberRow.get("points")).intValue();
        Integer currentLevel = memberRow.get("level") != null ? ((Number) memberRow.get("level")).intValue() : null;
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .le(MemberLevel::getPointsRequired, currentPoints)
                        .orderByDesc(MemberLevel::getPointsRequired));
        int newLevel = levels.isEmpty() ? 1 : levels.get(0).getId().intValue();
        if (currentLevel == null || newLevel != currentLevel) {
            int levelRows = jdbcTemplate.update("UPDATE member SET level = ? WHERE id = ?", newLevel, memberId);
            if (levelRows == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退款后降级失败");
            }
            log.info("退款后会员降级: id={}, fromLevel={}, toLevel={}, points={}",
                memberId, currentLevel, newLevel, currentPoints);
        }

        order.setStatus(2);
        log.info("退款成功: orderId={}, memberId={}, refundBalance={}, refundPoints={}, couponRestored={}",
                id, order.getMemberId(), order.getBalanceUsed(), order.getPointsEarned(), order.getCouponId() != null);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumptionOrder suspendOrder(SuspendOrderReqDTO dto) {
        // 1. 校验会员
        Member member = memberMapper.selectById(dto.getMemberId());
        if (member == null) {
            throw BusinessException.notFound("会员不存在，id=" + dto.getMemberId());
        }

        // 2. 获取会员等级折扣率
        BigDecimal discountRate = BigDecimal.ONE;
        if (member.getLevel() != null && member.getLevel() > 0) {
            MemberLevel memberLevel = memberLevelMapper.selectById(member.getLevel().longValue());
            if (memberLevel != null && memberLevel.getDiscountRate() != null) {
                discountRate = memberLevel.getDiscountRate();
            }
        }

        // 3. 计算原总金额
        BigDecimal originalTotal = dto.getItems().stream()
                .map(item -> item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 应用等级折扣
        BigDecimal totalAmount = originalTotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

        // 5. 创建挂单（status=0）
        ConsumptionOrder order = new ConsumptionOrder();
        order.setMemberId(dto.getMemberId());
        order.setTotalAmount(totalAmount);
        order.setPointsEarned(totalAmount.setScale(0, RoundingMode.HALF_UP).intValue());
        order.setStatus(0);
        order.setEmployeeId(dto.getEmployeeId());

        // 6. 计算提成
        BigDecimal commissionAmount = BigDecimal.ZERO;
        if (dto.getEmployeeId() != null) {
            List<Long> itemIds = dto.getItems().stream()
                    .map(SuspendOrderReqDTO.OrderItemDTO::getItemId).distinct().collect(Collectors.toList());
            if (!itemIds.isEmpty()) {
                String placeholders = itemIds.stream().map(id -> "?").collect(Collectors.joining(","));
                List<Map<String, Object>> commRows = jdbcTemplate.queryForList(
                    "SELECT id, commission_type, commission_value FROM service_item WHERE id IN (" + placeholders + ")",
                    itemIds.toArray());
                Map<Long, Map<String, Object>> commMap = commRows.stream()
                    .collect(Collectors.toMap(r -> ((Number) r.get("id")).longValue(), r -> r));
                for (SuspendOrderReqDTO.OrderItemDTO itemDTO : dto.getItems()) {
                    Map<String, Object> row = commMap.get(itemDTO.getItemId());
                    if (row != null) {
                        Number ct = (Number) row.get("commission_type");
                        Number cv = (Number) row.get("commission_value");
                        if (ct != null && cv != null && ct.intValue() > 0) {
                            if (ct.intValue() == 1) {
                                commissionAmount = commissionAmount.add(
                                    new BigDecimal(cv.toString()).multiply(BigDecimal.valueOf(itemDTO.getQuantity()))
                                        .setScale(2, RoundingMode.HALF_UP));
                            } else if (ct.intValue() == 2) {
                                commissionAmount = commissionAmount.add(
                                    itemDTO.getItemPrice().multiply(new BigDecimal(cv.toString()))
                                        .multiply(BigDecimal.valueOf(itemDTO.getQuantity()))
                                        .setScale(2, RoundingMode.HALF_UP));
                            }
                        }
                    }
                }
            }
        }
        order.setCommissionAmount(commissionAmount);
        baseMapper.insert(order);

        // 7. 批量插入明细
        List<ConsumptionOrderItem> orderItems = new ArrayList<>();
        for (SuspendOrderReqDTO.OrderItemDTO itemDTO : dto.getItems()) {
            ConsumptionOrderItem item = new ConsumptionOrderItem();
            item.setOrderId(order.getId());
            item.setItemId(itemDTO.getItemId());
            item.setItemName(itemDTO.getItemName());
            item.setItemPrice(itemDTO.getItemPrice());
            item.setQuantity(itemDTO.getQuantity());
            orderItems.add(item);
        }
        List<Object[]> suspendBatchArgs = new ArrayList<>();
        for (ConsumptionOrderItem oi : orderItems) {
            suspendBatchArgs.add(new Object[]{oi.getOrderId(), oi.getItemId(), oi.getItemName(), oi.getItemPrice(), oi.getQuantity()});
        }
        int[] suspendResults = jdbcTemplate.batchUpdate(
            "INSERT INTO consumption_order_item (order_id, item_id, item_name, item_price, quantity) VALUES (?, ?, ?, ?, ?)",
            suspendBatchArgs);
        int suspendSuccess = 0;
        for (int r : suspendResults) { if (r == 1 || r == java.sql.Statement.SUCCESS_NO_INFO) suspendSuccess++; }
        if (suspendSuccess != orderItems.size())
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "挂单明细批量写入失败");

        log.info("挂单成功: orderId={}, memberId={}, totalAmount={}, itemCount={}",
                order.getId(), dto.getMemberId(), totalAmount, dto.getItems().size());
        return order;
    }

    @Override
    public List<SuspendedOrderVO> listSuspended() {
        List<ConsumptionOrder> orders = baseMapper.selectList(
                new LambdaQueryWrapper<ConsumptionOrder>()
                        .eq(ConsumptionOrder::getStatus, 0)
                        .orderByDesc(ConsumptionOrder::getCreateTime));
        List<SuspendedOrderVO> result = new ArrayList<>();
        for (ConsumptionOrder order : orders) {
            Long count = orderItemMapper.selectCount(
                    new LambdaQueryWrapper<ConsumptionOrderItem>()
                            .eq(ConsumptionOrderItem::getOrderId, order.getId()));
            result.add(SuspendedOrderVO.from(order, count.intValue()));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumptionOrder resumeOrder(Long id, ResumeOrderReqDTO dto) {
        ConsumptionOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        if (Integer.valueOf(1).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态异常，无法结算");
        }

        BigDecimal totalAmount = order.getTotalAmount();

        // 更新支付信息
        order.setPayMethod(dto.getPayMethod());
        order.setPayAmount(dto.getPayAmount());
        order.setBalanceUsed(dto.getBalanceUsed() != null ? dto.getBalanceUsed() :
                (Integer.valueOf(2).equals(dto.getPayMethod()) ? totalAmount : null));
        order.setPayRemark(dto.getPayRemark());
        if (dto.getEmployeeId() != null) {
            order.setEmployeeId(dto.getEmployeeId());
        }

        // 组合支付
        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            BigDecimal totalPayments = dto.getPayments().stream()
                    .map(CreateOrderDTO.PaymentDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            if (totalPayments.compareTo(totalAmount) != 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                    String.format("支付金额合计(%.2f)与订单金额(%.2f)不一致", totalPayments, totalAmount));
            }
            order.setPayMethod(8);
            BigDecimal balanceDeduct = BigDecimal.ZERO;
            List<PaymentDetail> paymentDetails = new ArrayList<>();
            for (CreateOrderDTO.PaymentDTO p : dto.getPayments()) {
                PaymentDetail detail = new PaymentDetail();
                detail.setOrderId(order.getId());
                detail.setPayMethod(p.getPayMethod());
                detail.setAmount(p.getAmount());
                detail.setPayChannel(PayMethodEnum.of(p.getPayMethod()).name());
                paymentDetails.add(detail);
                if (Integer.valueOf(2).equals(p.getPayMethod())) {
                    balanceDeduct = balanceDeduct.add(p.getAmount());
                }
            }
            paymentDetailService.batchInsert(paymentDetails);
            order.setPayAmount(totalPayments.subtract(balanceDeduct).setScale(2, RoundingMode.HALF_UP));
            order.setBalanceUsed(balanceDeduct.setScale(2, RoundingMode.HALF_UP));
        }

        // 优惠券核销
        if (dto.getCouponCode() != null && !dto.getCouponCode().isBlank()) {
            List<Map<String, Object>> couponRows = jdbcTemplate.queryForList(
                "SELECT id, status, discount_value, condition_amount, expire_time FROM coupon WHERE code = ?",
                dto.getCouponCode());
            if (couponRows.isEmpty()) {
                throw new BusinessException(ErrorCode.COUPON_NOT_FOUND);
            }
            Map<String, Object> c = couponRows.get(0);
            int cStatus = (int) c.get("status");
            if (cStatus == 2) throw new BusinessException(ErrorCode.COUPON_USED);
            if (cStatus == 3) throw new BusinessException(ErrorCode.COUPON_EXPIRED);
            Object expireRaw = c.get("expire_time");
            LocalDateTime expireTime = expireRaw instanceof LocalDateTime ? (LocalDateTime) expireRaw
                : expireRaw instanceof java.sql.Timestamp ? ((java.sql.Timestamp) expireRaw).toLocalDateTime()
                : null;
            if (expireTime != null && expireTime.isBefore(LocalDateTime.now(ZONE))) {
                throw new BusinessException(ErrorCode.COUPON_EXPIRED);
            }
            BigDecimal conditionAmount = (BigDecimal) c.get("condition_amount");
            if (conditionAmount != null && conditionAmount.compareTo(BigDecimal.ZERO) > 0
                    && totalAmount.compareTo(conditionAmount) < 0) {
                throw new BusinessException(ErrorCode.COUPON_THRESHOLD_NOT_MET,
                    String.format("未满足门槛，需满 ¥%.2f", conditionAmount));
            }
            BigDecimal discountValue = (BigDecimal) c.get("discount_value");
            BigDecimal discount = discountValue != null ? discountValue : BigDecimal.ZERO;
            if (discount.compareTo(totalAmount) > 0) discount = totalAmount;
            int updateRows = jdbcTemplate.update(
                "UPDATE coupon SET status = 2, use_time = ?, order_id = ? WHERE id = ? AND status = 1",
                LocalDateTime.now(ZONE), order.getId(), ((Number) c.get("id")).longValue());
            if (updateRows == 0) throw new BusinessException(ErrorCode.COUPON_USED, "优惠券已使用或已失效");
            totalAmount = totalAmount.subtract(discount).max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
            order.setCouponId(((Number) c.get("id")).longValue());
            order.setCouponDiscount(discount);
            order.setTotalAmount(totalAmount);
        }

        // 原子更新会员余额、积分、消费总额、访次
        Member member = memberMapper.selectById(order.getMemberId());
        BigDecimal balanceToDeduct = order.getBalanceUsed() != null ? order.getBalanceUsed() : BigDecimal.ZERO;
        int memberRows;
        if (balanceToDeduct.compareTo(BigDecimal.ZERO) > 0) {
            memberRows = jdbcTemplate.update(
                "UPDATE member SET balance = balance - ?, points = points + ?, "
                + "total_spent = total_spent + ?, visit_count = visit_count + 1, last_consume_date = ? "
                + "WHERE id = ? AND balance >= ?",
                balanceToDeduct, order.getPointsEarned(), order.getTotalAmount(),
                LocalDate.now(ZONE), member.getId(), balanceToDeduct);
            if (memberRows == 0) throw new BusinessException(ErrorCode.MEMBER_BALANCE_INSUFFICIENT);
        } else {
            memberRows = jdbcTemplate.update(
                "UPDATE member SET points = points + ?, total_spent = total_spent + ?, "
                + "visit_count = visit_count + 1, last_consume_date = ? WHERE id = ?",
                order.getPointsEarned(), order.getTotalAmount(), LocalDate.now(ZONE), member.getId());
            if (memberRows == 0) throw new BusinessException(ErrorCode.BAD_REQUEST, "更新会员失败");
        }

        // 等级自动升级
        member.setBalance(member.getBalance().subtract(balanceToDeduct));
        member.setPoints(member.getPoints() + order.getPointsEarned());
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .le(MemberLevel::getPointsRequired, member.getPoints())
                        .orderByDesc(MemberLevel::getPointsRequired));
        if (!levels.isEmpty()) {
            int newLevel = levels.get(0).getId().intValue();
            if (member.getLevel() == null || newLevel != member.getLevel()) {
                int levelRows = jdbcTemplate.update("UPDATE member SET level = ? WHERE id = ?", newLevel, member.getId());
                if (levelRows == 0) {
                    throw new BusinessException(ErrorCode.INTERNAL_ERROR, "会员升级失败");
                }
                log.info("会员升级: id={}, fromLevel={}, toLevel={}, points={}",
                    member.getId(), member.getLevel(), newLevel, member.getPoints());
            }
        }

        // 原子状态转换：0 → 1
        int statusRows = jdbcTemplate.update(
            "UPDATE consumption_order SET status = 1, pay_method = ?, pay_amount = ?, balance_used = ?, "
            + "pay_remark = ?, employee_id = ?, coupon_id = ?, coupon_discount = ?, total_amount = ? "
            + "WHERE id = ? AND status = 0",
            order.getPayMethod(), order.getPayAmount(), order.getBalanceUsed(),
            order.getPayRemark(), order.getEmployeeId(), order.getCouponId(),
            order.getCouponDiscount(), order.getTotalAmount(), id);
        if (statusRows == 0) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID, "订单状态已变更，请刷新重试");
        }
        order.setStatus(1);

        log.info("取单结算成功: orderId={}, memberId={}, totalAmount={}, payMethod={}",
                order.getId(), order.getMemberId(), order.getTotalAmount(), order.getPayMethod());
        return order;
    }
}
