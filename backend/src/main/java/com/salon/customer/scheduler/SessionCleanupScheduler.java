package com.salon.customer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 客户会话定期清理。
 * <p>
 * 每天凌晨 5:00 清理已过期的 session，防止 customer_session 表无限增长。
 * SQL WHERE 条件自身提供幂等保证。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 5 * * *")
    public void cleanupExpiredSessions() {
        Boolean locked = jdbcTemplate.queryForObject("SELECT GET_LOCK('session_cleanup', 0)", Boolean.class);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("SessionCleanupScheduler: lock not acquired, skip");
            return;
        }
        try {
            int rows = jdbcTemplate.update(
                "DELETE FROM customer_session WHERE expire_at < ?", LocalDateTime.now());
            if (rows > 0) {
                log.info("SessionCleanupScheduler: {} 条过期session已清理", rows);
            }
        } finally {
            jdbcTemplate.execute("SELECT RELEASE_LOCK('session_cleanup')");
        }
    }
}
