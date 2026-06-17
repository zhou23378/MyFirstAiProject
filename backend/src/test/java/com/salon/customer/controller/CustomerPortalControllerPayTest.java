package com.salon.customer.controller;

import com.salon.common.exception.BusinessException;
import com.salon.common.result.Result;
import com.salon.customer.dto.CustomerPayReqDTO;
import com.salon.customer.entity.CustomerSession;
import com.salon.customer.mapper.CustomerSessionMapper;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CustomerPortalControllerPayTest {

    @Autowired
    private CustomerPortalController customerPortalController;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private CustomerSessionMapper sessionMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long memberId;
    private String token;

    @BeforeEach
    void setUp() {
        ensureRechargeRecordTable();
        jdbcTemplate.execute("DELETE FROM recharge_record");
        jdbcTemplate.execute("DELETE FROM customer_session");
        jdbcTemplate.execute("DELETE FROM member");

        Member member = new Member();
        member.setName("余额支付会员");
        member.setPhone("13800009999");
        member.setBalance(new BigDecimal("100.00"));
        memberMapper.insert(member);
        memberId = member.getId();

        token = "customer-pay-token";
        CustomerSession session = new CustomerSession();
        session.setMemberId(memberId);
        session.setToken(token);
        session.setLoginMethod("SMS_CODE");
        session.setExpireAt(LocalDateTime.now().plusDays(1));
        sessionMapper.insert(session);
    }

    @Test
    void pay_shouldDeductBalanceAndCreateNegativeRecord() {
        Result<Map<String, Object>> result = customerPortalController.pay(token, payReq("30.126"));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().get("balance")).isEqualTo(new BigDecimal("69.87"));

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM member WHERE id = ?", BigDecimal.class, memberId);
        assertThat(balance).isEqualByComparingTo("69.87");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM recharge_record WHERE member_id = ?", Integer.class, memberId);
        assertThat(count).isEqualTo(1);

        BigDecimal amount = jdbcTemplate.queryForObject(
                "SELECT amount FROM recharge_record WHERE member_id = ?", BigDecimal.class, memberId);
        assertThat(amount).isEqualByComparingTo("-30.13");
    }

    @Test
    void pay_shouldRollbackRecordWhenBalanceInsufficient() {
        CustomerPayReqDTO req = payReq("120.00");

        assertThatThrownBy(() -> customerPortalController.pay(token, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("余额不足");

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM member WHERE id = ?", BigDecimal.class, memberId);
        assertThat(balance).isEqualByComparingTo("100.00");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM recharge_record WHERE member_id = ?", Integer.class, memberId);
        assertThat(count).isZero();
    }

    @Test
    void pay_shouldRollbackBalanceWhenRecordWriteFails() {
        jdbcTemplate.execute("DROP TABLE recharge_record");

        assertThatThrownBy(() -> customerPortalController.pay(token, payReq("30.00")))
                .isInstanceOf(Exception.class);

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM member WHERE id = ?", BigDecimal.class, memberId);
        assertThat(balance).isEqualByComparingTo("100.00");

        ensureRechargeRecordTable();
    }

    private CustomerPayReqDTO payReq(String amount) {
        CustomerPayReqDTO req = new CustomerPayReqDTO();
        req.setAmount(new BigDecimal(amount));
        req.setPayMethod("BALANCE");
        return req;
    }

    private void ensureRechargeRecordTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS recharge_record (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  member_id BIGINT NOT NULL,
                  amount DECIMAL(10,2) NOT NULL,
                  pay_method TINYINT DEFAULT 1,
                  remark VARCHAR(255) DEFAULT NULL,
                  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }
}
