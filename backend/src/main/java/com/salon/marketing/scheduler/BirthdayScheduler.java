package com.salon.marketing.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salon.coupon.entity.Coupon;
import com.salon.coupon.entity.CouponTemplate;
import com.salon.coupon.mapper.CouponMapper;
import com.salon.coupon.mapper.CouponTemplateMapper;
import com.salon.marketing.entity.BirthdayConfig;
import com.salon.marketing.mapper.BirthdayConfigMapper;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.notification.enums.NotificationType;
import com.salon.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BirthdayScheduler {

    private final MemberMapper memberMapper;
    private final CouponMapper couponMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final BirthdayConfigMapper birthdayConfigMapper;
    private final SmsService smsService;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void processBirthdayMembers() {
        Boolean locked = jdbcTemplate.queryForObject("SELECT GET_LOCK('birthday_scheduler', 0)", Boolean.class);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("BirthdayScheduler: lock not acquired, skip");
            return;
        }
        try {
            doProcessBirthdayMembers();
        } finally {
            jdbcTemplate.execute("SELECT RELEASE_LOCK('birthday_scheduler')");
        }
    }

    private void doProcessBirthdayMembers() {
        log.info("BirthdayScheduler: scanning birthday members...");
        LocalDate today = LocalDate.now();

        BirthdayConfig config = birthdayConfigMapper.selectById(1);
        if (config == null || Integer.valueOf(0).equals(config.getEnabled()) || config.getCouponTemplateId() == null) {
            log.info("BirthdayScheduler: not configured or disabled, skip");
            return;
        }

        CouponTemplate template = couponTemplateMapper.selectById(config.getCouponTemplateId());
        if (template == null) {
            log.warn("BirthdayScheduler: template id={} not found, skip", config.getCouponTemplateId());
            return;
        }

        List<Member> members = memberMapper.selectList(
            new LambdaQueryWrapper<Member>()
                .isNotNull(Member::getBirthday)
                .apply("MONTH(birthday) = {0} AND DAY(birthday) = {1}", today.getMonthValue(), today.getDayOfMonth()));

        for (Member m : members) {
            try {
                // 幂等检查：今天是否已发过生日券（同一模板+同一会员+今天）
                Long alreadyIssued = couponMapper.selectCount(
                    new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getMemberId, m.getId())
                        .eq(Coupon::getTemplateId, template.getId())
                        .ge(Coupon::getReceiveTime, today.atStartOfDay()));
                if (alreadyIssued != null && alreadyIssued > 0) {
                    log.info("BirthdayScheduler: member {} already received today, skip", m.getId());
                    continue;
                }

                Coupon coupon = new Coupon();
                coupon.setMemberId(m.getId());
                coupon.setTemplateId(template.getId());
                coupon.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
                coupon.setStatus(1);
                coupon.setDiscountValue(template.getDiscountValue());
                coupon.setConditionAmount(template.getConditionAmount());
                coupon.setReceiveTime(LocalDateTime.now());
                int validDays = template.getValidDays() != null ? template.getValidDays() : 7;
                coupon.setExpireTime(LocalDateTime.now().plusDays(validDays));
                couponMapper.insert(coupon);

                if (Integer.valueOf(1).equals(config.getSmsEnabled()) && m.getPhone() != null && !m.getPhone().isEmpty()) {
                    smsService.send(m.getPhone(), NotificationType.BIRTHDAY.getType(), NotificationType.BIRTHDAY.getTemplateCode(),
                        "亲爱的" + m.getName() + "，祝您生日快乐！我们为您准备了生日礼券，欢迎到店使用。", m.getId());
                }
                log.info("BirthdayScheduler: processed member {} ({})", m.getName(), m.getId());
            } catch (Exception e) {
                log.error("BirthdayScheduler: error for member {}", m.getId(), e);
            }
        }
        log.info("BirthdayScheduler: done, {} members processed", members.size());
    }
}
