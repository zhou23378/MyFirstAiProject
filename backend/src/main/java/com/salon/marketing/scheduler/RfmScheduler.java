package com.salon.marketing.scheduler;

import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RfmScheduler {

    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 2 * * 1")
    public void calculateRfm() {
        Boolean locked = jdbcTemplate.queryForObject("SELECT GET_LOCK('rfm_scheduler', 0)", Boolean.class);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("RfmScheduler: lock not acquired, skip");
            return;
        }
        try {
            doCalculateRfm();
        } finally {
            jdbcTemplate.execute("SELECT RELEASE_LOCK('rfm_scheduler')");
        }
    }

    private void doCalculateRfm() {
        log.info("RfmScheduler: starting RFM calculation...");
        LocalDate today = LocalDate.now();

        List<Member> members = memberMapper.selectList(null);
        if (members.isEmpty()) return;

        List<Long> recencyDays = new ArrayList<>();
        List<Integer> frequencyList = new ArrayList<>();
        List<BigDecimal> monetaryList = new ArrayList<>();

        for (Member m : members) {
            LocalDate lastVisit = m.getLastVisitDate();
            long r = lastVisit != null ? ChronoUnit.DAYS.between(lastVisit, today) : 365;
            recencyDays.add(r);

            String sql = "SELECT COUNT(*) FROM consumption_order WHERE member_id = ? AND status = 1 AND create_time >= ?";
            Long f = jdbcTemplate.queryForObject(sql, Long.class, m.getId(), today.minusDays(90));
            frequencyList.add(f != null ? f.intValue() : 0);

            String sql2 = "SELECT COALESCE(SUM(total_amount), 0) FROM consumption_order WHERE member_id = ? AND status = 1";
            BigDecimal mVal = jdbcTemplate.queryForObject(sql2, BigDecimal.class, m.getId());
            monetaryList.add(mVal != null ? mVal : BigDecimal.ZERO);
        }

        double medianR = medianLong(recencyDays);
        double medianF = medianInt(frequencyList);
        double medianM = medianDecimal(monetaryList);

        for (int i = 0; i < members.size(); i++) {
            try {
                Member m = members.get(i);
                boolean highR = recencyDays.get(i) <= medianR;
                boolean highF = frequencyList.get(i) >= medianF;
                boolean highM = monetaryList.get(i).compareTo(BigDecimal.valueOf(medianM)) >= 0;

                String segment;
                if (highR && highF && highM) segment = "CHAMPIONS";
                else if (highR && highF && !highM) segment = "LOYAL";
                else if (highR && !highF && highM) segment = "POTENTIAL";
                else if (highR && !highF && !highM) segment = "NEW";
                else if (!highR && highF && highM) segment = "AT_RISK";
                else if (!highR && highF && !highM) segment = "NEED_ATTENTION";
                else if (!highR && !highF && highM) segment = "DORMANT_HIGH";
                else segment = "LOST";

                // 仅更新 rfm_segment，不覆盖 total_spent/visit_count（由 ConsumptionOrderServiceImpl 原子维护）
                int rows = jdbcTemplate.update(
                    "UPDATE member SET rfm_segment = ? WHERE id = ?",
                    segment, m.getId());
                if (rows == 0) {
                    log.warn("RfmScheduler: member {} not found, skip", m.getId());
                }
            } catch (Exception e) {
                log.error("RfmScheduler: error for member {}", members.get(i).getId(), e);
            }
        }
        log.info("RfmScheduler: done, {} members categorized", members.size());
    }

    private double medianLong(List<Long> list) {
        List<Long> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        int mid = sorted.size() / 2;
        return sorted.size() % 2 == 0 ? (sorted.get(mid - 1) + sorted.get(mid)) / 2.0 : sorted.get(mid);
    }

    private double medianInt(List<Integer> list) {
        List<Integer> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        int mid = sorted.size() / 2;
        return sorted.size() % 2 == 0 ? (sorted.get(mid - 1) + sorted.get(mid)) / 2.0 : sorted.get(mid);
    }

    private double medianDecimal(List<BigDecimal> list) {
        List<BigDecimal> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        int mid = sorted.size() / 2;
        return sorted.size() % 2 == 0
            ? sorted.get(mid - 1).add(sorted.get(mid)).divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP).doubleValue()
            : sorted.get(mid).doubleValue();
    }
}
