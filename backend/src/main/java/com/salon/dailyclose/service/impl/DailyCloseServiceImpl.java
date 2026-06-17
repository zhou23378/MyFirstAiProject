package com.salon.dailyclose.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.dailyclose.dto.DailyCloseReqDTO;
import com.salon.dailyclose.entity.DailyClose;
import com.salon.dailyclose.mapper.DailyCloseMapper;
import com.salon.dailyclose.service.DailyCloseService;
import com.salon.dailyclose.vo.DailyCloseSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyCloseServiceImpl extends ServiceImpl<DailyCloseMapper, DailyClose> implements DailyCloseService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public DailyCloseSummaryVO getTodaySummary() {
        LocalDate today = LocalDate.now();
        String sql = """
            SELECT
                COALESCE(SUM(CASE WHEN pay_method = 1 THEN pay_amount + balance_used ELSE 0 END), 0) AS cash,
                COALESCE(SUM(CASE WHEN pay_method = 2 THEN balance_used ELSE 0 END), 0) AS balance,
                COALESCE(SUM(CASE WHEN pay_method = 3 THEN pay_amount ELSE 0 END), 0) AS wechat,
                COALESCE(SUM(CASE WHEN pay_method = 4 THEN pay_amount ELSE 0 END), 0) AS alipay,
                COALESCE(SUM(CASE WHEN pay_method = 5 THEN pay_amount ELSE 0 END), 0) AS bankCard,
                COALESCE(SUM(CASE WHEN pay_method = 6 THEN pay_amount ELSE 0 END), 0) AS card,
                COALESCE(SUM(CASE WHEN pay_method = 7 THEN pay_amount ELSE 0 END), 0) AS groupon,
                COALESCE(SUM(CASE WHEN pay_method = 8 THEN pay_amount ELSE 0 END), 0) AS mixedPay,
                COALESCE(SUM(pay_amount + balance_used), 0) AS total
            FROM consumption_order
            WHERE status = 1 AND DATE(create_time) = ?
            """;
        Map<String, Object> summary = jdbcTemplate.queryForMap(sql, today);

        DailyCloseSummaryVO result = new DailyCloseSummaryVO();
        result.setCloseDate(today.toString());
        result.setSystemCash((BigDecimal) summary.get("cash"));
        result.setSystemBalance((BigDecimal) summary.get("balance"));
        result.setSystemWechat((BigDecimal) summary.get("wechat"));
        result.setSystemAlipay((BigDecimal) summary.get("alipay"));
        result.setSystemBankCard((BigDecimal) summary.get("bankCard"));
        result.setSystemCard((BigDecimal) summary.get("card"));
        result.setSystemGroupon((BigDecimal) summary.get("groupon"));
        result.setSystemMixedPay((BigDecimal) summary.get("mixedPay"));
        result.setSystemTotal((BigDecimal) summary.get("total"));

        DailyClose existing = getOne(new LambdaQueryWrapper<DailyClose>().eq(DailyClose::getCloseDate, today));
        if (existing != null) {
            result.setId(existing.getId());
            result.setManualCash(existing.getManualCash());
            result.setManualWechat(existing.getManualWechat());
            result.setManualAlipay(existing.getManualAlipay());
            result.setManualTotal(existing.getManualTotal());
            result.setDiffAmount(existing.getDiffAmount());
            result.setStatus(existing.getStatus());
            result.setRemark(existing.getRemark());
            result.setLockedBy(existing.getLockedBy());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyClose submitEntry(DailyCloseReqDTO dto) {
        LocalDate date = dto.getCloseDate();

        DailyClose existing = getOne(new LambdaQueryWrapper<DailyClose>().eq(DailyClose::getCloseDate, date));
        if (existing == null) {
            DailyClose entity = new DailyClose();
            entity.setCloseDate(date);
            entity.setManualCash(dto.getManualCash());
            entity.setManualWechat(dto.getManualWechat());
            entity.setManualAlipay(dto.getManualAlipay());
            entity.setRemark(dto.getRemark());
            populateSystemTotals(entity, date);
            recalcSummary(entity);
            save(entity);
            log.info("日结新建: id={}, closeDate={}, systemTotal={}", entity.getId(), date, entity.getSystemTotal());
            return entity;
        } else {
            existing.setManualCash(dto.getManualCash());
            existing.setManualWechat(dto.getManualWechat());
            existing.setManualAlipay(dto.getManualAlipay());
            existing.setRemark(dto.getRemark());
            recalcSummary(existing);
            // 原子更新：仅当未锁定时才写入，防止 TOCTOU 绕过锁定
            int rows = jdbcTemplate.update(
                "UPDATE daily_close SET manual_cash = ?, manual_wechat = ?, manual_alipay = ?, " +
                "remark = ?, system_total = ?, manual_total = ?, diff_amount = ? " +
                "WHERE id = ? AND (status IS NULL OR status = 0)",
                existing.getManualCash(), existing.getManualWechat(), existing.getManualAlipay(),
                existing.getRemark(), existing.getSystemTotal(), existing.getManualTotal(), existing.getDiffAmount(),
                existing.getId());
            if (rows == 0) {
                throw BusinessException.badRequest("该日期日结已锁定，无法修改");
            }
            log.info("日结更新: id={}, closeDate={}, manualTotal={}", existing.getId(), date, existing.getManualTotal());
            return existing;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyClose lock(Long id, String lockedBy) {
        DailyClose entity = getById(id);
        if (entity == null) throw BusinessException.notFound("日结记录不存在");
        recalcSummary(entity);

        LocalDateTime now = LocalDateTime.now();
        int rows = jdbcTemplate.update(
            "UPDATE daily_close SET system_total = ?, manual_total = ?, diff_amount = ?, " +
            "status = 1, locked_by = ?, locked_at = ? WHERE id = ? AND (status IS NULL OR status = 0)",
            entity.getSystemTotal(), entity.getManualTotal(), entity.getDiffAmount(),
            lockedBy, now, id);
        if (rows == 0) {
            throw BusinessException.badRequest("已锁定或记录不存在");
        }
        entity.setStatus(1);
        entity.setLockedBy(lockedBy);
        entity.setLockedAt(now);
        log.info("日结锁定: id={}, lockedBy={}, diffAmount={}", id, lockedBy, entity.getDiffAmount());
        return entity;
    }

    private void populateSystemTotals(DailyClose entity, LocalDate date) {
        String sql = """
            SELECT
                COALESCE(SUM(CASE WHEN pay_method = 1 THEN pay_amount + balance_used ELSE 0 END), 0) AS cash,
                COALESCE(SUM(CASE WHEN pay_method = 2 THEN balance_used ELSE 0 END), 0) AS balance,
                COALESCE(SUM(CASE WHEN pay_method = 3 THEN pay_amount ELSE 0 END), 0) AS wechat,
                COALESCE(SUM(CASE WHEN pay_method = 4 THEN pay_amount ELSE 0 END), 0) AS alipay,
                COALESCE(SUM(CASE WHEN pay_method = 5 THEN pay_amount ELSE 0 END), 0) AS bankCard,
                COALESCE(SUM(CASE WHEN pay_method = 6 THEN pay_amount ELSE 0 END), 0) AS card,
                COALESCE(SUM(CASE WHEN pay_method = 7 THEN pay_amount ELSE 0 END), 0) AS groupon,
                COALESCE(SUM(CASE WHEN pay_method = 8 THEN pay_amount ELSE 0 END), 0) AS mixedPay
            FROM consumption_order
            WHERE status = 1 AND DATE(create_time) = ?
            """;
        Map<String, Object> summary = jdbcTemplate.queryForMap(sql, date);
        entity.setSystemCash((BigDecimal) summary.get("cash"));
        entity.setSystemBalance((BigDecimal) summary.get("balance"));
        entity.setSystemWechat((BigDecimal) summary.get("wechat"));
        entity.setSystemAlipay((BigDecimal) summary.get("alipay"));
        entity.setSystemBankCard((BigDecimal) summary.get("bankCard"));
        entity.setSystemCard((BigDecimal) summary.get("card"));
        entity.setSystemGroupon((BigDecimal) summary.get("groupon"));
        entity.setSystemMixedPay((BigDecimal) summary.get("mixedPay"));
    }

    private void recalcSummary(DailyClose entity) {
        BigDecimal total = BigDecimal.ZERO;
        if (entity.getSystemCash() != null) total = total.add(entity.getSystemCash());
        if (entity.getSystemWechat() != null) total = total.add(entity.getSystemWechat());
        if (entity.getSystemAlipay() != null) total = total.add(entity.getSystemAlipay());
        if (entity.getSystemBalance() != null) total = total.add(entity.getSystemBalance());
        if (entity.getSystemBankCard() != null) total = total.add(entity.getSystemBankCard());
        if (entity.getSystemCard() != null) total = total.add(entity.getSystemCard());
        if (entity.getSystemGroupon() != null) total = total.add(entity.getSystemGroupon());
        if (entity.getSystemMixedPay() != null) total = total.add(entity.getSystemMixedPay());
        entity.setSystemTotal(total.setScale(2, java.math.RoundingMode.HALF_UP));

        BigDecimal manualTotal = BigDecimal.ZERO;
        if (entity.getManualCash() != null) manualTotal = manualTotal.add(entity.getManualCash());
        if (entity.getManualWechat() != null) manualTotal = manualTotal.add(entity.getManualWechat());
        if (entity.getManualAlipay() != null) manualTotal = manualTotal.add(entity.getManualAlipay());
        entity.setManualTotal(manualTotal.compareTo(BigDecimal.ZERO) > 0
            ? manualTotal.setScale(2, java.math.RoundingMode.HALF_UP) : null);

        if (entity.getManualTotal() != null) {
            entity.setDiffAmount(total.subtract(entity.getManualTotal())
                .setScale(2, java.math.RoundingMode.HALF_UP));
        } else {
            entity.setDiffAmount(BigDecimal.ZERO);
        }
    }
}
