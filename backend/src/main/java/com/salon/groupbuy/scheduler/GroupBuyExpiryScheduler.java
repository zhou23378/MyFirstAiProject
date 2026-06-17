package com.salon.groupbuy.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyExpiryScheduler {

    private static final String LOCK_SQL = "SELECT GET_LOCK('group_buy_expiry', 0)";
    private static final String RELEASE_LOCK_SQL = "SELECT RELEASE_LOCK('group_buy_expiry')";
    private static final String EXPIRE_ORDER_SQL =
        "UPDATE group_buy_order SET status = 4 WHERE status = 1 AND expire_time < NOW()";
    private static final String REFUND_PARTICIPANT_SQL =
        "UPDATE group_buy_participant SET status = 4 " +
            "WHERE status = 1 AND order_id IN (SELECT id FROM group_buy_order WHERE status = 4)";
    private static final String REFUND_QUERY_SQL =
        "SELECT DISTINCT p.id, p.member_id, p.join_price FROM group_buy_participant p " +
            "INNER JOIN group_buy_order o ON p.order_id = o.id " +
            "WHERE p.status = 4 AND o.status = 4 AND p.refund_time IS NULL";
    private static final String REFUND_REMARK = "拼团退款-超时未成团";
    private static final int REFUND_PAY_METHOD = 5;

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void expireOrders() {
        Boolean locked = jdbcTemplate.queryForObject(LOCK_SQL, Boolean.class);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("GroupBuyExpiryScheduler: lock not acquired, skip");
            return;
        }

        boolean releaseInFinally = !releaseLockAfterTransaction();
        try {
            doExpireOrders();
        } finally {
            if (releaseInFinally) {
                releaseLock();
            }
        }
    }

    private boolean releaseLockAfterTransaction() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                releaseLock();
            }
        });
        return true;
    }

    private void releaseLock() {
        jdbcTemplate.execute(RELEASE_LOCK_SQL);
    }

    private void doExpireOrders() {
        int expiredCount = jdbcTemplate.update(EXPIRE_ORDER_SQL);
        if (expiredCount > 0) {
            log.info("拼团过期: {} 个团单已过期", expiredCount);
        }

        int refundParticipantCount = jdbcTemplate.update(REFUND_PARTICIPANT_SQL);
        if (refundParticipantCount > 0) {
            log.info("拼团退款标记: {} 条参团记录待退款", refundParticipantCount);
        }

        List<Map<String, Object>> toRefund = jdbcTemplate.queryForList(REFUND_QUERY_SQL);
        for (Map<String, Object> row : toRefund) {
            Long participantId = ((Number) row.get("id")).longValue();
            Long memberId = ((Number) row.get("member_id")).longValue();
            BigDecimal joinPrice = (BigDecimal) row.get("join_price");
            try {
                int recordRows = jdbcTemplate.update(
                    "INSERT INTO recharge_record (member_id, amount, pay_method, remark) VALUES (?, ?, ?, ?)",
                    memberId, joinPrice, REFUND_PAY_METHOD, REFUND_REMARK);
                if (recordRows == 0) {
                    log.error("拼团退款流水插入失败: participantId={}, memberId={}", participantId, memberId);
                    continue;
                }

                int refundRows = jdbcTemplate.update(
                    "UPDATE member SET balance = balance + ? WHERE id = ?", joinPrice, memberId);
                if (refundRows == 0) {
                    log.error("拼团退款余额更新失败(会员可能已删除): participantId={}, memberId={}", participantId, memberId);
                    continue;
                }

                int markRows = jdbcTemplate.update(
                    "UPDATE group_buy_participant SET refund_time = NOW() WHERE id = ? AND refund_time IS NULL",
                    participantId);
                if (markRows == 0) {
                    log.error("拼团退款标记失败: participantId={}, memberId={}", participantId, memberId);
                    continue;
                }

                log.info("拼团退款成功: participantId={}, memberId={}, amount={}", participantId, memberId, joinPrice);
            } catch (Exception e) {
                log.error("拼团退款失败: participantId={}, memberId={}", participantId, memberId, e);
            }
        }
    }
}
