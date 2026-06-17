package com.salon.marketing.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.tag.entity.MemberTag;
import com.salon.tag.entity.TagRule;
import com.salon.tag.service.MemberTagService;
import com.salon.tag.service.TagRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagRuleScheduler {

    private final TagRuleService tagRuleService;
    private final MemberTagService memberTagService;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(cron = "0 0 3 * * *")
    public void executeTagRules() {
        Boolean locked = jdbcTemplate.queryForObject("SELECT GET_LOCK('tag_rule_scheduler', 0)", Boolean.class);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("TagRuleScheduler: lock not acquired, skip");
            return;
        }
        try {
            doExecuteTagRules();
        } finally {
            jdbcTemplate.execute("SELECT RELEASE_LOCK('tag_rule_scheduler')");
        }
    }

    private void doExecuteTagRules() {
        log.info("TagRuleScheduler: executing tag rules...");
        List<TagRule> rules = tagRuleService.list(
            new LambdaQueryWrapper<TagRule>().eq(TagRule::getEnabled, 1));

        for (TagRule rule : rules) {
            try {
                List<Map<String, Object>> conditions = objectMapper.readValue(
                    rule.getConditionsJson(), new TypeReference<List<Map<String, Object>>>() {});

                List<Member> members = memberMapper.selectList(null);
                for (Member m : members) {
                    try {
                        if (matchesAllConditions(m, conditions)) {
                            MemberTag existing = memberTagService.getOne(
                                new LambdaQueryWrapper<MemberTag>()
                                    .eq(MemberTag::getMemberId, m.getId())
                                    .eq(MemberTag::getTagName, rule.getTagName()));
                            if (existing == null) {
                                MemberTag tag = new MemberTag();
                                tag.setMemberId(m.getId());
                                tag.setTagName(rule.getTagName());
                                tag.setSource("AUTO");
                                tag.setRuleId(rule.getId());
                                memberTagService.save(tag);
                            }
                        }
                    } catch (Exception innerE) {
                        log.error("TagRuleScheduler: error for member {} in rule '{}'", m.getId(), rule.getName(), innerE);
                    }
                }
                log.info("TagRuleScheduler: rule '{}' executed", rule.getName());
            } catch (Exception e) {
                log.error("TagRuleScheduler: error executing rule '{}'", rule.getName(), e);
            }
        }
        log.info("TagRuleScheduler: done, {} rules executed", rules.size());
    }

    private boolean matchesAllConditions(Member m, List<Map<String, Object>> conditions) {
        if (conditions == null || conditions.isEmpty()) return false;
        for (Map<String, Object> cond : conditions) {
            if (!matchesCondition(m, cond)) return false;
        }
        return true;
    }

    private boolean matchesCondition(Member m, Map<String, Object> cond) {
        String field = (String) cond.get("field");
        String op = (String) cond.get("op");
        double value = ((Number) cond.get("value")).doubleValue();

        double actual;
        switch (field) {
            case "last_visit_days":
                actual = m.getLastVisitDate() != null
                    ? ChronoUnit.DAYS.between(m.getLastVisitDate(), LocalDate.now()) : 999;
                break;
            case "total_spent":
                actual = m.getTotalSpent() != null ? m.getTotalSpent().doubleValue() : 0;
                break;
            case "visit_count":
                actual = m.getVisitCount() != null ? m.getVisitCount() : 0;
                break;
            case "balance":
                actual = m.getBalance() != null ? m.getBalance().doubleValue() : 0;
                break;
            case "level":
                actual = m.getLevel() != null ? m.getLevel() : 0;
                break;
            default: return false;
        }
        return switch (op) {
            case ">" -> actual > value;
            case "<" -> actual < value;
            case ">=" -> actual >= value;
            case "<=" -> actual <= value;
            case "=", "==" -> Math.abs(actual - value) < 0.01;
            case "!=" -> Math.abs(actual - value) >= 0.01;
            default -> false;
        };
    }
}
