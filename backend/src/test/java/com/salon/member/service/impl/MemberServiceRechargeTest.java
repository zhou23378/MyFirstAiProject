package com.salon.member.service.impl;

import com.salon.member.dto.RechargeDTO;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.member.service.MemberService;
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
class MemberServiceRechargeTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long memberId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM recharge_record");
        jdbcTemplate.execute("DELETE FROM member");
        Member m = new Member();
        m.setName("测试会员");
        m.setPhone("13800000001");
        m.setBalance(new BigDecimal("500.00"));
        memberMapper.insert(m);
        memberId = m.getId();
    }

    @Test
    void recharge_shouldIncreaseBalanceAndCreateRecord() {
        RechargeDTO dto = new RechargeDTO();
        dto.setAmount(new BigDecimal("100.00"));
        dto.setPayMethod(2);

        Member result = memberService.recharge(memberId, dto);

        assertThat(result.getBalance()).isEqualByComparingTo("600.00");

        // 验证流水记录已写入
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM recharge_record WHERE member_id = ?", Integer.class, memberId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void recharge_shouldThrowWhenMemberNotFound() {
        RechargeDTO dto = new RechargeDTO();
        dto.setAmount(new BigDecimal("100.00"));
        dto.setPayMethod(1);

        assertThatThrownBy(() -> memberService.recharge(99999L, dto))
            .hasMessageContaining("会员不存在");
    }

    @Test
    void recharge_shouldHandleMultipleConsecutiveRecharges() {
        RechargeDTO dto1 = new RechargeDTO();
        dto1.setAmount(new BigDecimal("100.00"));
        dto1.setPayMethod(2);
        memberService.recharge(memberId, dto1);

        RechargeDTO dto2 = new RechargeDTO();
        dto2.setAmount(new BigDecimal("50.00"));
        dto2.setPayMethod(3);
        memberService.recharge(memberId, dto2);

        Member member = memberMapper.selectById(memberId);
        assertThat(member.getBalance()).isEqualByComparingTo("650.00");
    }
}
