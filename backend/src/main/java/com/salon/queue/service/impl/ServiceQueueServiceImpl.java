package com.salon.queue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.queue.entity.ServiceQueue;
import com.salon.queue.mapper.ServiceQueueMapper;
import com.salon.queue.service.ServiceQueueService;
import com.salon.queue.vo.QueueTodayStatsVO;
import com.salon.queue.vo.ServiceQueueItemVO;
import com.salon.technician.mapper.TechnicianStatusMapper;
import com.salon.timer.entity.ServiceTimer;
import com.salon.timer.mapper.ServiceTimerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceQueueServiceImpl extends ServiceImpl<ServiceQueueMapper, ServiceQueue> implements ServiceQueueService {

    private final TechnicianStatusMapper technicianStatusMapper;
    private final ServiceTimerMapper serviceTimerMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public List<ServiceQueueItemVO> listToday() {
        List<ServiceQueue> queues = list(
            new LambdaQueryWrapper<ServiceQueue>()
                .ge(ServiceQueue::getCreateTime, LocalDate.now().atStartOfDay())
                .orderByAsc(ServiceQueue::getQueueNumber));

        List<ServiceQueueItemVO> result = new ArrayList<>();
        for (ServiceQueue q : queues) {
            ServiceQueueItemVO vo = new ServiceQueueItemVO();
            vo.setId(q.getId());
            vo.setMemberId(q.getMemberId());
            vo.setMemberName(q.getMemberName());
            vo.setServiceItemId(q.getServiceItemId());
            vo.setServiceItemName(q.getServiceItemName());
            vo.setQueueNumber(q.getQueueNumber());
            vo.setStatus(q.getStatus());
            vo.setAssignedEmployeeId(q.getAssignedEmployeeId());
            vo.setWaitSince(q.getWaitSince());
            if (q.getWaitSince() != null) {
                vo.setWaitMinutes(ChronoUnit.MINUTES.between(q.getWaitSince(), LocalDateTime.now(ZONE)));
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceQueue enqueue(Long memberId, String memberName, Long serviceItemId, String serviceItemName) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        String sql = "INSERT INTO service_queue (member_id, member_name, service_item_id, service_item_name, " +
            "queue_number, status, wait_since) " +
            "SELECT ?, ?, ?, ?, COALESCE(MAX(queue_number), 0) + 1, 1, ? " +
            "FROM service_queue WHERE create_time >= ?";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, memberId);
            ps.setString(2, memberName);
            if (serviceItemId != null) ps.setLong(3, serviceItemId); else ps.setNull(3, Types.BIGINT);
            ps.setString(4, serviceItemName);
            ps.setObject(5, now);
            ps.setObject(6, LocalDate.now(ZONE).atStartOfDay());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "排队取号失败");
        return getById(key.longValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long queueId, Long employeeId) {
        // 原子更新排队状态：只有 status=1 时才分配成功
        int queueRows = jdbcTemplate.update(
            "UPDATE service_queue SET status = 2, assigned_employee_id = ? WHERE id = ? AND status = 1",
            employeeId, queueId);
        if (queueRows == 0) throw BusinessException.badRequest("该排队已处理或不存在");

        // 原子更新技师状态：只有 AVAILABLE 时才设为 BUSY
        int techRows = jdbcTemplate.update(
            "UPDATE technician_status SET status = 'BUSY', current_customer_name = " +
            "(SELECT member_name FROM service_queue WHERE id = ?), " +
            "current_service_name = (SELECT service_item_name FROM service_queue WHERE id = ?), " +
            "last_status_changed = ? WHERE employee_id = ? AND status = 'AVAILABLE'",
            queueId, queueId, LocalDateTime.now(ZONE), employeeId);
        if (techRows == 0) {
            // 回滚排队分配（允许静默失败：行可能已被其他操作修改）
            int rollbackRows = jdbcTemplate.update(
                "UPDATE service_queue SET status = 1, assigned_employee_id = NULL WHERE id = ?", queueId);
            if (rollbackRows == 0) {
                log.warn("排队回滚失败（状态已变更）: queueId={}", String.valueOf(queueId));
            }
            throw BusinessException.badRequest("技师当前不可用");
        }

        // A4: jdbcTemplate 写后禁止用 MyBatis 读同一实体，改用 jdbcTemplate 读取必要字段
        Map<String, Object> qRow = jdbcTemplate.queryForMap(
            "SELECT member_id, service_item_id, service_item_name FROM service_queue WHERE id = ?", queueId);
        ServiceTimer timer = new ServiceTimer();
        timer.setQueueId(queueId);
        timer.setEmployeeId(employeeId);
        timer.setMemberId(((Number) qRow.get("member_id")).longValue());
        timer.setServiceItemId(qRow.get("service_item_id") != null ? ((Number) qRow.get("service_item_id")).longValue() : null);
        timer.setServiceItemName(qRow.get("service_item_name") != null ? (String) qRow.get("service_item_name") : "未指定");
        timer.setPlannedDuration(30);
        timer.setStatus(1);
        timer.setStartedAt(LocalDateTime.now(ZONE));
        serviceTimerMapper.insert(timer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void skip(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE service_queue SET status = 4 WHERE id = ? AND status = 1", id);
        if (rows == 0) throw BusinessException.badRequest("只能跳过等待中的排队");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE service_queue SET status = 3 WHERE id = ? AND status = 1", id);
        if (rows == 0) throw BusinessException.badRequest("只能取消等待中的排队");
    }

    @Override
    public ServiceQueue findNextWaitingToday() {
        List<ServiceQueue> list = baseMapper.selectList(
            new LambdaQueryWrapper<ServiceQueue>()
                .eq(ServiceQueue::getStatus, 1)
                .ge(ServiceQueue::getCreateTime, LocalDate.now().atStartOfDay())
                .orderByAsc(ServiceQueue::getQueueNumber)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public QueueTodayStatsVO todayStats() {
        long waiting = count(new LambdaQueryWrapper<ServiceQueue>()
            .eq(ServiceQueue::getStatus, 1)
            .ge(ServiceQueue::getCreateTime, LocalDate.now().atStartOfDay()));
        long assigned = count(new LambdaQueryWrapper<ServiceQueue>()
            .eq(ServiceQueue::getStatus, 2)
            .ge(ServiceQueue::getCreateTime, LocalDate.now().atStartOfDay()));
        QueueTodayStatsVO stats = new QueueTodayStatsVO();
        stats.setWaiting((int) waiting);
        stats.setAssigned((int) assigned);
        stats.setTotal((int) (waiting + assigned));
        return stats;
    }
}
