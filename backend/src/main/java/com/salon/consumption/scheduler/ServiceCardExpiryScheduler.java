package com.salon.consumption.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 次卡过期定时任务
 * <p>
 * 每天凌晨 4:30 执行，将已过期的有效次卡批量标记为 status=3。
 * SQL WHERE 条件自身提供幂等保证。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceCardExpiryScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 30 4 * * *")
    public void expireServiceCards() {
        Boolean locked = jdbcTemplate.queryForObject("SELECT GET_LOCK('svc_card_expiry', 0)", Boolean.class);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("ServiceCardExpiryScheduler: lock not acquired, skip");
            return;
        }
        try {
            int rows = jdbcTemplate.update(
                "UPDATE service_card SET status = 3 WHERE status = 1 AND expire_date IS NOT NULL AND expire_date < ?",
                LocalDate.now());
            if (rows > 0) {
                log.info("ServiceCardExpiryScheduler: {} 张次卡已过期", rows);
            }
        } finally {
            jdbcTemplate.execute("SELECT RELEASE_LOCK('svc_card_expiry')");
        }
    }
}
