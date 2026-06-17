package com.salon.groupbuy.service.impl;

import com.salon.groupbuy.dto.GroupBuyOrderReqDTO;
import com.salon.groupbuy.service.GroupBuyOrderService;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 并发安全测试：验证原子 SQL 在高并发下的一致性。
 * 规则 A1-A3 / H1-H4 覆盖。
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrentSafetyTest {

    @Autowired
    private GroupBuyOrderService groupBuyOrderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberMapper memberMapper;

    private Long member1Id;
    private Long member2Id;
    private Long templateId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM group_buy_participant");
        jdbcTemplate.execute("DELETE FROM group_buy_order");
        jdbcTemplate.execute("DELETE FROM group_buy_template");
        jdbcTemplate.execute("DELETE FROM recharge_record");
        jdbcTemplate.execute("DELETE FROM member");

        member1Id = newMember("团长", "111", "200.00");
        member2Id = newMember("团员A", "222", "200.00");

        // 2人团模板
        jdbcTemplate.update(
            "INSERT INTO group_buy_template (id, name, original_price, group_price, group_size, expire_hours, total_qty, issued_qty, status) VALUES (1, '2人团', 68.00, 29.90, 2, 24, 10, 0, 1)");
        templateId = 1L;
    }

    private Long newMember(String name, String phone, String balance) {
        Member m = new Member();
        m.setName(name);
        m.setPhone(phone);
        m.setBalance(new BigDecimal(balance));
        memberMapper.insert(m);
        return m.getId();
    }

    @Test
    void concurrentJoin_lastSlot_onlyOneSucceeds() throws Exception {
        // 开团 (current_size=1, 只剩1个名额)
        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);
        Long orderId = groupBuyOrderService.create(member1Id, req).getId();

        // 2个会员同时参团
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Runnable joinTask = () -> {
            try {
                groupBuyOrderService.join(orderId, member2Id);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        Thread t1 = new Thread(joinTask);
        Thread t2 = new Thread(joinTask);
        t1.start();
        t2.start();
        latch.await();

        // 恰好一个成功，一个失败（满员）
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        // 团单成团
        Integer status = jdbcTemplate.queryForObject(
            "SELECT status FROM group_buy_order WHERE id = ?", Integer.class, orderId);
        assertThat(status).isEqualTo(2); // 已成团
    }

    @Test
    void concurrentJoin_sameMember_onlyOneSucceeds() throws Exception {
        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);
        Long orderId = groupBuyOrderService.create(member1Id, req).getId();

        // 同一会员并发参团（防重复参团）
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);

        Runnable joinTask = () -> {
            try {
                groupBuyOrderService.join(orderId, member2Id);
                successCount.incrementAndGet();
            } catch (Exception e) {
                // expected for duplicate
            } finally {
                latch.countDown();
            }
        };

        new Thread(joinTask).start();
        new Thread(joinTask).start();
        latch.await();

        assertThat(successCount.get()).isEqualTo(1);
    }
}
