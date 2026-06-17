package com.salon.consumption.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.consumption.entity.ServiceCard;
import com.salon.consumption.mapper.ServiceCardMapper;
import com.salon.consumption.service.ServiceCardService;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCardServiceImpl extends ServiceImpl<ServiceCardMapper, ServiceCard> implements ServiceCardService {

    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public ServiceCard purchase(ServiceCard card) {
        Member member = memberMapper.selectById(card.getMemberId());
        if (member == null) throw BusinessException.notFound("会员不存在");
        BigDecimal memberBalance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
        if (memberBalance.compareTo(card.getPrice()) < 0) {
            throw new BusinessException(ErrorCode.MEMBER_BALANCE_INSUFFICIENT);
        }
        // 未传名称时从服务项目表获取
        if (card.getName() == null || card.getName().isBlank()) {
            String name = jdbcTemplate.queryForObject(
                "SELECT name FROM service_item WHERE id = ?", String.class, card.getServiceItemId());
            card.setName(name != null ? name : "未知服务");
        }
        // 先写流水记录，再变更余额（规则 F2：流水在余额变更之前）
        card.setUsedCount(0);
        card.setStatus(1);
        save(card);

        // 原子扣减余额，防并发读写覆盖
        int rows = jdbcTemplate.update(
            "UPDATE member SET balance = balance - ? WHERE id = ? AND balance >= ?",
            card.getPrice(), card.getMemberId(), card.getPrice());
        if (rows == 0) throw new BusinessException(ErrorCode.MEMBER_BALANCE_INSUFFICIENT);

        log.info("次卡购买: memberId={}, cardId={}, price={}, newBalance={}",
            card.getMemberId(), card.getId(), card.getPrice(), memberBalance.subtract(card.getPrice()));
        return card;
    }

    @Override
    @Transactional
    public ServiceCard deduct(Long id) {
        ServiceCard card = getById(id);
        if (card == null) {
            throw BusinessException.notFound("次卡不存在");
        }
        if (!Integer.valueOf(1).equals(card.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "次卡已失效");
        }
        if (card.getUsedCount() >= card.getTotalCount()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "次卡次数已用完");
        }
        if (card.getExpireDate() != null && card.getExpireDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "次卡已过期");
        }
        // 原子更新：只有未用完时才扣减成功，防并发超扣
        int rows = jdbcTemplate.update(
            "UPDATE service_card SET used_count = used_count + 1, " +
            "status = IF(used_count + 1 >= total_count, 2, status) " +
            "WHERE id = ? AND status = 1 AND used_count < total_count",
            id);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "次卡已用完或已失效");
        }
        card.setUsedCount(card.getUsedCount() + 1);
        if (card.getUsedCount() >= card.getTotalCount()) card.setStatus(2);
        log.info("次卡扣次: cardId={}, usedCount={}/{}, memberId={}",
            id, card.getUsedCount(), card.getTotalCount(), card.getMemberId());
        return card;
    }
}
