package com.salon.groupbuy.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyExpiryScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void expireOrders() {
        // 1. Expire orders: atomic UPDATE WHERE status=1 AND expire_time < NOW() (E1 idempotent)
        int expiredCount = jdbcTemplate.update(
            "UPDATE group_buy_order SET status = 4 WHERE status = 1 AND expire_time < NOW()");
        if (expiredCount > 0) {
            log.info("拼团过期: {} 个团单已过期", expiredCount);
        }

        // 2. Mark participants for refund: atomic UPDATE WHERE status=1 (E1 idempotent)
        //    Do NOT set refund_time here — refund_time is set after actual refund (R1 fix)
        int refundParticipantCount = jdbcTemplate.update(
            "UPDATE group_buy_participant SET status = 4 " +
            "WHERE status = 1 AND order_id IN (SELECT id FROM group_buy_order WHERE status = 4)");
        if (refundParticipantCount > 0) {
            log.info("拼团退款标记: {} 条参团记录待退款", refundParticipantCount);
        }

        // 3. Refund balance per participant (E2: per-record try-catch)
        //    Query only unrefunded records (refund_time IS NULL) for idempotency (R1)
        List<Map<String, Object>> toRefund = jdbcTemplate.queryForList(
            "SELECT DISTINCT p.id, p.member_id, p.join_price FROM group_buy_participant p " +
            "INNER JOIN group_buy_order o ON p.order_id = o.id " +
            "WHERE p.status = 4 AND o.status = 4 AND p.refund_time IS NULL");

        for (Map<String, Object> row : toRefund) {
            Long participantId = ((Number) row.get("id")).longValue();
            Long memberId = ((Number) row.get("member_id")).longValue();
            BigDecimal joinPrice = (BigDecimal) row.get("join_price");
            try {
                // Insert balance record BEFORE balance change (F2 rule)
                int recordRows = jdbcTemplate.update(
                    "INSERT INTO recharge_record (member_id, amount, pay_method, remark) VALUES (?, ?, 5, ?)",
                    memberId, joinPrice, "拼团退款-超时未成团");
                if (recordRows == 0) {
                    log.error("拼团退款流水插入失败: participantId={}, memberId={}", participantId, memberId);
                    continue;
                }

                // Atomic refund: add balance back (A1)
                int refundRows = jdbcTemplate.update(
                    "UPDATE member SET balance = balance + ? WHERE id = ?", joinPrice, memberId);
                if (refundRows == 0) {
                    log.error("拼团退款余额更新失败(会员可能已删除): participantId={}, memberId={}", participantId, memberId);
                    continue;
                }

                // Mark refund as processed (R1: idempotency guard)
                jdbcTemplate.update(
                    "UPDATE group_buy_participant SET refund_time = NOW() WHERE id = ? AND refund_time IS NULL",
                    participantId);

                log.info("拼团退款成功: participantId={}, memberId={}, amount={}", participantId, memberId, joinPrice);
            } catch (Exception e) {
                log.error("拼团退款失败: participantId={}, memberId={}, error={}",
                    participantId, memberId, e.getMessage());
            }
        }
    }
}
