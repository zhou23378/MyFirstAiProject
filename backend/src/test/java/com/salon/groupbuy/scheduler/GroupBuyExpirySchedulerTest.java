package com.salon.groupbuy.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupBuyExpirySchedulerTest {

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

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private GroupBuyExpiryScheduler scheduler;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void expireOrders_shouldSkipWhenLockNotAcquired() {
        when(jdbcTemplate.queryForObject(eq(LOCK_SQL), eq(Boolean.class))).thenReturn(false);

        scheduler.expireOrders();

        verify(jdbcTemplate).queryForObject(eq(LOCK_SQL), eq(Boolean.class));
        verify(jdbcTemplate, never()).update(eq(EXPIRE_ORDER_SQL));
        verify(jdbcTemplate, never()).execute(eq(RELEASE_LOCK_SQL));
    }

    @Test
    void expireOrders_shouldReleaseLockAfterTransactionCompletion() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            when(jdbcTemplate.queryForObject(eq(LOCK_SQL), eq(Boolean.class))).thenReturn(true);
            when(jdbcTemplate.update(eq(EXPIRE_ORDER_SQL))).thenReturn(1);
            when(jdbcTemplate.update(eq(REFUND_PARTICIPANT_SQL))).thenReturn(0);
            when(jdbcTemplate.queryForList(eq(REFUND_QUERY_SQL))).thenReturn(Collections.emptyList());

            scheduler.expireOrders();

            verify(jdbcTemplate).queryForObject(eq(LOCK_SQL), eq(Boolean.class));
            verify(jdbcTemplate).update(eq(EXPIRE_ORDER_SQL));
            verify(jdbcTemplate).update(eq(REFUND_PARTICIPANT_SQL));
            verify(jdbcTemplate, never()).execute(eq(RELEASE_LOCK_SQL));

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

            verify(jdbcTemplate).execute(eq(RELEASE_LOCK_SQL));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void expireOrders_shouldReleaseLockWhenRefundFlowFailsOutsideTransaction() {
        when(jdbcTemplate.queryForObject(eq(LOCK_SQL), eq(Boolean.class))).thenReturn(true);
        when(jdbcTemplate.update(eq(EXPIRE_ORDER_SQL))).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> scheduler.expireOrders())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");

        verify(jdbcTemplate).execute(eq(RELEASE_LOCK_SQL));
    }

    @Test
    void expireOrders_shouldReleaseLockAfterRolledBackTransaction() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            when(jdbcTemplate.queryForObject(eq(LOCK_SQL), eq(Boolean.class))).thenReturn(true);
            when(jdbcTemplate.update(eq(EXPIRE_ORDER_SQL))).thenThrow(new IllegalStateException("boom"));

            assertThatThrownBy(() -> scheduler.expireOrders())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

            verify(jdbcTemplate, never()).execute(eq(RELEASE_LOCK_SQL));

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

            verify(jdbcTemplate).execute(eq(RELEASE_LOCK_SQL));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
