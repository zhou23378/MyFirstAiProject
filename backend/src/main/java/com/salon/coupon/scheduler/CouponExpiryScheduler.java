package com.salon.coupon.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 优惠券过期定时任务
 * <p>
 * 每天凌晨 4:00 执行，将已过期的未使用优惠券批量标记为 status=3。
 * SQL WHERE 条件自身提供幂等保证，重复执行不会产生副作用。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpiryScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 4 * * *")
    public void expireCoupons() {
        Boolean locked = jdbcTemplate.queryForObject("SELECT GET_LOCK('coupon_expiry', 0)", Boolean.class);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("CouponExpiryScheduler: lock not acquired, skip");
            return;
        }
        try {
            int rows = jdbcTemplate.update(
                "UPDATE coupon SET status = 3 WHERE status = 1 AND expire_time < ?",
                LocalDateTime.now());
            if (rows > 0) {
                log.info("CouponExpiryScheduler: {} 张优惠券已过期", rows);
            }
        } finally {
            jdbcTemplate.execute("SELECT RELEASE_LOCK('coupon_expiry')");
        }
    }
}
