package com.salon.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.coupon.entity.Coupon;
import com.salon.coupon.entity.CouponTemplate;
import com.salon.coupon.mapper.CouponMapper;
import com.salon.coupon.mapper.CouponTemplateMapper;
import com.salon.coupon.service.CouponService;
import com.salon.coupon.vo.CouponPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CouponTemplateMapper templateMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public Page<CouponPageVO> pageWithNames(int page, int pageSize, Long memberId, Integer status) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (memberId != null) {
            where.append(" AND c.member_id = ?");
            params.add(memberId);
        }
        if (status != null) {
            where.append(" AND c.status = ?");
            params.add(status);
        }

        String countSql = "SELECT COUNT(*) FROM coupon c WHERE 1=1" + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);
        String dataSql = "SELECT c.id, c.template_id AS templateId, c.member_id AS memberId, " +
                "c.code, c.status, c.discount_value AS discountValue, c.condition_amount AS conditionAmount, " +
                "c.receive_time AS receiveTime, c.use_time AS useTime, c.expire_time AS expireTime, " +
                "c.order_id AS orderId, c.create_time AS createTime, c.update_time AS updateTime, " +
                "ct.name AS templateName, ct.type AS templateType, m.name AS memberName " +
                "FROM coupon c " +
                "LEFT JOIN coupon_template ct ON c.template_id = ct.id " +
                "LEFT JOIN member m ON c.member_id = m.id " +
                "WHERE 1=1" + where +
                " ORDER BY c.create_time DESC LIMIT ?, ?";
        List<CouponPageVO> list = jdbcTemplate.query(dataSql, new BeanPropertyRowMapper<>(CouponPageVO.class), params.toArray());

        Page<CouponPageVO> result = new Page<>(page, pageSize);
        result.setTotal(total != null ? total : 0);
        result.setRecords(list);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon issue(Long templateId, Long memberId) {
        CouponTemplate template = templateMapper.selectById(templateId);
        if (template == null) throw new BusinessException(ErrorCode.COUPON_NOT_FOUND);
        if (Integer.valueOf(0).equals(template.getStatus())) throw new BusinessException(ErrorCode.COUPON_DISABLED);

        // 原子自增，避免并发超发：只有 issuedQty < totalQty 时才 +1 成功
        int rows = jdbcTemplate.update(
            "UPDATE coupon_template SET issued_qty = issued_qty + 1 WHERE id = ? AND (total_qty = 0 OR issued_qty < total_qty)",
            templateId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
        }

        Coupon coupon = new Coupon();
        coupon.setTemplateId(templateId);
        coupon.setMemberId(memberId);
        coupon.setCode(generateCode());
        coupon.setStatus(1);
        coupon.setDiscountValue(template.getDiscountValue());
        coupon.setConditionAmount(template.getConditionAmount());
        coupon.setReceiveTime(LocalDateTime.now(ZONE));
        coupon.setExpireTime(LocalDateTime.now(ZONE).plusDays(template.getValidDays()));

        save(coupon);
        log.info("优惠券发放: couponId={}, templateId={}, memberId={}, code={}",
            coupon.getId(), templateId, memberId, coupon.getCode());
        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal verify(String code, Long orderId, BigDecimal orderAmount) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getCode, code);
        Coupon coupon = getOne(wrapper);
        if (coupon == null) throw new BusinessException(ErrorCode.COUPON_NOT_FOUND);

        // Check status
        if (Integer.valueOf(2).equals(coupon.getStatus())) throw new BusinessException(ErrorCode.COUPON_USED);
        if (Integer.valueOf(3).equals(coupon.getStatus())) throw new BusinessException(ErrorCode.COUPON_EXPIRED);
        if (coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now(ZONE))) {
            int expireRows = jdbcTemplate.update(
                "UPDATE coupon SET status = 3 WHERE id = ? AND status = 1", coupon.getId());
            if (expireRows == 0) {
                log.warn("优惠券自动过期失败（状态已变更）: couponId={}", String.valueOf(coupon.getId()));
            }
            throw new BusinessException(ErrorCode.COUPON_EXPIRED);
        }

        // Check condition
        if (coupon.getConditionAmount() != null && coupon.getConditionAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (orderAmount.compareTo(coupon.getConditionAmount()) < 0) {
                throw new BusinessException(ErrorCode.COUPON_THRESHOLD_NOT_MET,
                    String.format("未满足门槛，需满 ¥%.2f", coupon.getConditionAmount()));
            }
        }

        // Calculate discount
        CouponTemplate template = templateMapper.selectById(coupon.getTemplateId());
        BigDecimal discount = BigDecimal.ZERO;
        if (template != null) {
            if (template.getType() == 1 || template.getType() == 3) {
                // Fixed amount off
                discount = coupon.getDiscountValue();
            } else if (template.getType() == 2) {
                // Percentage off
                discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountValue()));
            }
            discount = discount.setScale(2, java.math.RoundingMode.HALF_UP);
            if (discount.compareTo(orderAmount) > 0) discount = orderAmount;
        }

        // 原子更新：只有 status = 1 时才更新成功，防并发核销
        int rows = jdbcTemplate.update(
            "UPDATE coupon SET status = 2, use_time = ?, order_id = ? WHERE id = ? AND status = 1",
            LocalDateTime.now(ZONE), orderId, coupon.getId());
        if (rows == 0) {
            throw new BusinessException(ErrorCode.COUPON_USED, "优惠券已使用或已失效");
        }

        log.info("优惠券核销: couponId={}, orderId={}, discount={}, code={}",
            coupon.getId(), orderId, discount, code);
        return discount;
    }

    private String generateCode() {
        return "CP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
