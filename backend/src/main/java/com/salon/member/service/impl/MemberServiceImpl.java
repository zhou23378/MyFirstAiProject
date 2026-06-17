package com.salon.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.member.dto.MemberQueryDTO;
import com.salon.member.dto.MemberReqDTO;
import com.salon.member.dto.RechargeDTO;
import com.salon.member.entity.Member;
import com.salon.member.entity.RechargeRecord;
import com.salon.member.mapper.MemberMapper;
import com.salon.member.mapper.RechargeRecordMapper;
import com.salon.member.service.MemberService;
import com.salon.memberlevel.entity.MemberLevel;
import com.salon.memberlevel.mapper.MemberLevelMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 会员 Service 实现
 * <p>
 * 实现会员的增删改查业务逻辑，包含积分变更时自动升级等级
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private final MemberLevelMapper memberLevelMapper;
    private final RechargeRecordMapper rechargeRecordMapper;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 分页查询会员列表
     * <p>
     * 支持按姓名和手机号模糊查询，按创建时间倒序排列
     * </p>
     */
    @Override
    public IPage<Member> pageQuery(MemberQueryDTO query) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        // 按姓名模糊查询
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Member::getName, query.getName());
        }
        // 按手机号模糊查询
        if (StringUtils.hasText(query.getPhone())) {
            wrapper.like(Member::getPhone, query.getPhone());
        }
        // 余额范围
        if (query.getBalanceMin() != null) {
            wrapper.ge(Member::getBalance, query.getBalanceMin());
        }
        if (query.getBalanceMax() != null) {
            wrapper.le(Member::getBalance, query.getBalanceMax());
        }
        // 等级精确匹配
        if (query.getLevel() != null) {
            wrapper.eq(Member::getLevel, query.getLevel());
        }
        // 性别精确匹配
        if (query.getGender() != null) {
            wrapper.eq(Member::getGender, query.getGender());
        }
        // 累计消费金额范围（子查询 consumption_order）
        if (query.getTotalConsumeMin() != null) {
            wrapper.apply("(SELECT COALESCE(SUM(total_amount), 0) FROM consumption_order WHERE member_id = member.id) >= {0}",
                    query.getTotalConsumeMin());
        }
        if (query.getTotalConsumeMax() != null) {
            wrapper.apply("(SELECT COALESCE(SUM(total_amount), 0) FROM consumption_order WHERE member_id = member.id) <= {0}",
                    query.getTotalConsumeMax());
        }
        // 最后消费距今天数（>N 天未消费，含从未消费的）
        if (query.getLastConsumeDays() != null) {
            wrapper.and(w -> w
                    .lt(Member::getLastConsumeDate, LocalDate.now(ZONE).minusDays(query.getLastConsumeDays()))
                    .or().isNull(Member::getLastConsumeDate));
        }
        // 按创建时间倒序
        wrapper.orderByDesc(Member::getCreateTime);

        Page<Member> page = Page.of(query.getPage(), query.getPageSize());
        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * 新增会员
     * <p>
     * 校验手机号唯一性后保存，自动设置初始等级
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member createMember(MemberReqDTO req) {
        // 校验手机号是否已存在
        if (StringUtils.hasText(req.getPhone())) {
            checkPhoneUnique(null, req.getPhone());
        }

        Member member = new Member();
        BeanUtils.copyProperties(req, member);
        // 自动设置初始等级（最低等级）
        if (member.getLevel() == null || member.getLevel() == 0) {
            MemberLevel lowestLevel = getLowestLevel();
            if (lowestLevel != null) {
                member.setLevel(lowestLevel.getId().intValue());
            }
        }
        save(member);
        log.info("新增会员成功: id={}, name={}, phone={}", member.getId(), member.getName(),
                member.getPhone() != null ? member.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : null);
        return member;
    }

    /**
     * 修改会员
     * <p>
     * 校验会员存在性和手机号唯一性后更新
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member updateMember(Long id, MemberReqDTO req) {
        Member member = baseMapper.selectById(id);
        if (member == null) {
            throw BusinessException.notFound("会员不存在，id=" + id);
        }

        // 校验手机号是否已被其他会员使用
        if (StringUtils.hasText(req.getPhone())) {
            checkPhoneUnique(id, req.getPhone());
        }

        BeanUtils.copyProperties(req, member);
        updateById(member);
        log.info("修改会员成功: id={}, name={}", id, member.getName());
        return member;
    }

    /**
     * 删除会员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long id) {
        Member member = baseMapper.selectById(id);
        if (member == null) {
            throw BusinessException.notFound("会员不存在，id=" + id);
        }
        baseMapper.deleteById(id);
        log.info("删除会员成功: id={}, name={}", id, member.getName());
    }

    /**
     * 更新会员积分并自动升级等级
     * <p>
     * 在消费完成后调用，根据会员当前积分自动匹配对应的等级
     * </p>
     *
     * @param memberId 会员ID
     * @param points   新增积分
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPointsAndUpgrade(Long memberId, int points) {
        // 原子增加积分，避免并发 read-modify-write 覆盖
        int rows = jdbcTemplate.update(
            "UPDATE member SET points = points + ? WHERE id = ?", points, memberId);
        if (rows == 0) {
            throw BusinessException.notFound("会员不存在，id=" + memberId);
        }

        // 用 jdbcTemplate 读取最新值，绕过 MyBatis 一级缓存（规则 A4）
        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT points, level FROM member WHERE id = ?", memberId);
        int newPoints = ((Number) row.get("points")).intValue();
        Object levelObj = row.get("level");
        int currentLevel = levelObj != null ? ((Number) levelObj).intValue() : 0;

        // 自动升级：根据当前积分找到匹配的最高等级
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .le(MemberLevel::getPointsRequired, newPoints)
                        .orderByDesc(MemberLevel::getPointsRequired)
        );

        if (!levels.isEmpty()) {
            MemberLevel targetLevel = levels.get(0);
            if (!targetLevel.getId().equals(Long.valueOf(currentLevel))) {
                log.info("会员升级: id={}, fromLevel={}, toLevel={}, points={}",
                        memberId, currentLevel, targetLevel.getId(), newPoints);
                jdbcTemplate.update(
                    "UPDATE member SET level = ? WHERE id = ?", targetLevel.getId().intValue(), memberId);
            }
        }
    }

    /**
     * 获取最低等级
     */
    private MemberLevel getLowestLevel() {
        List<MemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<MemberLevel>()
                        .orderByAsc(MemberLevel::getPointsRequired)
                        .last("LIMIT 1")
        );
        return levels.isEmpty() ? null : levels.get(0);
    }

    /**
     * 校验手机号唯一性
     *
     * @param excludeId 排除的会员ID（修改时排除自身）
     * @param phone     手机号
     * @throws BusinessException 如果手机号已存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member recharge(Long memberId, RechargeDTO dto) {
        // 记录充值
        RechargeRecord record = new RechargeRecord();
        record.setMemberId(memberId);
        record.setAmount(dto.getAmount());
        record.setPayMethod(dto.getPayMethod());
        record.setRemark(dto.getRemark());
        rechargeRecordMapper.insert(record);

        // 先读会员（jdbcTemplate 写之前），再原子更新余额
        Member member = baseMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员不存在");
        }

        // 原子增加余额，防并发读写覆盖
        int rows = jdbcTemplate.update(
            "UPDATE member SET balance = balance + ? WHERE id = ?",
            dto.getAmount(), memberId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员不存在");
        }

        log.info("会员充值成功: memberId={}, amount={}", memberId, dto.getAmount());

        // 手动同步内存对象，避免 jdbcTemplate 写后用 MyBatis 读同一实体（规则 A4）
        member.setBalance(member.getBalance().add(dto.getAmount()));
        return member;
    }

    @Override
    public List<Member> getChurnRiskMembers(int days) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<Member>()
                        .lt(Member::getLastConsumeDate, LocalDate.now(ZONE).minusDays(days))
                        .or()
                        .isNull(Member::getLastConsumeDate)
                        .orderByAsc(Member::getLastConsumeDate)
        );
    }

    private void checkPhoneUnique(Long excludeId, String phone) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhone, phone);
        if (excludeId != null) {
            wrapper.ne(Member::getId, excludeId);
        }
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }
    }
}
