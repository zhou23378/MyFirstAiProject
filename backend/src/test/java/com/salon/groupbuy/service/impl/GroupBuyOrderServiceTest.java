package com.salon.groupbuy.service.impl;

import com.salon.groupbuy.dto.GroupBuyOrderReqDTO;
import com.salon.groupbuy.service.GroupBuyOrderService;
import com.salon.groupbuy.vo.GroupBuyOrderVO;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class GroupBuyOrderServiceTest {

    @Autowired
    private GroupBuyOrderService groupBuyOrderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberMapper memberMapper;

    private Long member1Id;
    private Long member2Id;
    private Long member3Id;
    private Long templateId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM group_buy_participant");
        jdbcTemplate.execute("DELETE FROM group_buy_order");
        jdbcTemplate.execute("DELETE FROM group_buy_template");
        jdbcTemplate.execute("DELETE FROM recharge_record");
        jdbcTemplate.execute("DELETE FROM member");

        // 创建 3 个测试会员
        Member m1 = newMember("团长", "13800000001", "500.00");
        Member m2 = newMember("团员A", "13800000002", "500.00");
        Member m3 = newMember("团员B", "13800000003", "500.00");
        member1Id = m1.getId();
        member2Id = m2.getId();
        member3Id = m3.getId();

        // 创建拼团模板
        jdbcTemplate.update(
            "INSERT INTO group_buy_template (id, name, original_price, group_price, group_size, expire_hours, total_qty, issued_qty, status) VALUES (1, '测试三人团', 68.00, 29.90, 3, 24, 10, 0, 1)");
        templateId = 1L;
    }

    private Member newMember(String name, String phone, String balance) {
        Member m = new Member();
        m.setName(name);
        m.setPhone(phone);
        m.setBalance(new BigDecimal(balance));
        memberMapper.insert(m);
        return m;
    }

    @Test
    void create_shouldCreateOrderAndDeductBalance() {
        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);

        GroupBuyOrderVO vo = groupBuyOrderService.create(member1Id, req);

        assertThat(vo).isNotNull();
        assertThat(vo.getLeaderId()).isEqualTo(member1Id);
        assertThat(vo.getCurrentSize()).isEqualTo(1);
        assertThat(vo.getStatus()).isEqualTo(1);

        // 余额已扣减
        Member m = memberMapper.selectById(member1Id);
        assertThat(m.getBalance()).isEqualByComparingTo("470.10");
    }

    @Test
    void create_shouldThrowWhenBalanceInsufficient() {
        // 将会员余额改为很少
        jdbcTemplate.update("UPDATE member SET balance = 1.00 WHERE id = ?", member1Id);

        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);

        assertThatThrownBy(() -> groupBuyOrderService.create(member1Id, req))
            .hasMessageContaining("余额不足");
    }

    @Test
    void join_shouldIncreaseCurrentSize() {
        // 先开团
        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);
        GroupBuyOrderVO created = groupBuyOrderService.create(member1Id, req);
        Long orderId = created.getId();
        assertThat(created.getCurrentSize()).isEqualTo(1);

        // 团员参团
        GroupBuyOrderVO joined = groupBuyOrderService.join(orderId, member2Id);

        assertThat(joined.getCurrentSize()).isEqualTo(2);
        assertThat(joined.getStatus()).isEqualTo(1); // 还未满员
    }

    @Test
    void join_shouldCompleteOrderWhenLastMemberJoins() {
        // 2人团模板
        jdbcTemplate.update("UPDATE group_buy_template SET group_size = 2 WHERE id = ?", templateId);

        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);
        GroupBuyOrderVO created = groupBuyOrderService.create(member1Id, req);
        Long orderId = created.getId();

        // 第二人参团 → 应立即成团
        GroupBuyOrderVO joined = groupBuyOrderService.join(orderId, member2Id);

        assertThat(joined.getCurrentSize()).isEqualTo(2);
        assertThat(joined.getStatus()).isEqualTo(2); // 已成团
    }

    @Test
    void join_shouldThrowWhenAlreadyJoined() {
        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);
        GroupBuyOrderVO created = groupBuyOrderService.create(member1Id, req);
        Long orderId = created.getId();

        groupBuyOrderService.join(orderId, member2Id);

        // 重复参团
        assertThatThrownBy(() -> groupBuyOrderService.join(orderId, member2Id))
            .hasMessageContaining("已参与");
    }

    @Test
    void join_shouldThrowWhenOrderFull() {
        // 2人团，团长+1团员即满
        jdbcTemplate.update("UPDATE group_buy_template SET group_size = 2 WHERE id = ?", templateId);

        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);
        GroupBuyOrderVO created = groupBuyOrderService.create(member1Id, req);
        Long orderId = created.getId();
        groupBuyOrderService.join(orderId, member2Id);

        // 第三人无法参团
        assertThatThrownBy(() -> groupBuyOrderService.join(orderId, member3Id))
            .hasMessageContaining("已结束");
    }

    @Test
    void fullFlow_threePersonGroup_completesSuccessfully() {
        GroupBuyOrderReqDTO req = new GroupBuyOrderReqDTO();
        req.setTemplateId(templateId);

        // 开团
        GroupBuyOrderVO order = groupBuyOrderService.create(member1Id, req);
        assertThat(order.getCurrentSize()).isEqualTo(1);

        // 团员A参团
        order = groupBuyOrderService.join(order.getId(), member2Id);
        assertThat(order.getCurrentSize()).isEqualTo(2);
        assertThat(order.getStatus()).isEqualTo(1);

        // 团员B参团 → 满员成团
        order = groupBuyOrderService.join(order.getId(), member3Id);
        assertThat(order.getCurrentSize()).isEqualTo(3);
        assertThat(order.getStatus()).isEqualTo(2);

        // 三人都扣了余额
        assertThat(memberMapper.selectById(member1Id).getBalance()).isEqualByComparingTo("470.10");
        assertThat(memberMapper.selectById(member2Id).getBalance()).isEqualByComparingTo("470.10");
        assertThat(memberMapper.selectById(member3Id).getBalance()).isEqualByComparingTo("470.10");
    }
}
