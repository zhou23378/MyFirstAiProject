package com.salon.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.consumption.dto.CreateOrderDTO;
import com.salon.consumption.service.ConsumptionOrderService;
import com.salon.service.entity.ServiceItem;
import com.salon.service.mapper.ServiceItemMapper;
import com.salon.technician.mapper.TechnicianStatusMapper;
import com.salon.timer.entity.ServiceTimer;
import com.salon.timer.event.TimerCompletedEvent;
import com.salon.timer.mapper.ServiceTimerMapper;
import com.salon.timer.service.ServiceTimerService;
import com.salon.timer.vo.ServiceTimerActiveVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceTimerServiceImpl extends ServiceImpl<ServiceTimerMapper, ServiceTimer> implements ServiceTimerService {

    private final TechnicianStatusMapper technicianStatusMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final ConsumptionOrderService consumptionOrderService;
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceTimer start(Long appointmentId, Long queueId, Long employeeId, Long memberId,
                              Long serviceItemId, String serviceItemName, Integer plannedDuration) {
        ServiceTimer timer = new ServiceTimer();
        timer.setAppointmentId(appointmentId);
        timer.setQueueId(queueId);
        timer.setEmployeeId(employeeId);
        timer.setMemberId(memberId);
        timer.setServiceItemId(serviceItemId);
        timer.setServiceItemName(serviceItemName);
        timer.setPlannedDuration(plannedDuration != null ? plannedDuration : 30);
        timer.setStatus(1);
        timer.setStartedAt(LocalDateTime.now(ZONE));
        save(timer);
        return timer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pause(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE service_timer SET status = 2, paused_at = ? WHERE id = ? AND status = 1",
            LocalDateTime.now(ZONE), id);
        if (rows == 0) throw BusinessException.badRequest("只能暂停进行中的计时");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resume(Long id) {
        // 读取暂停时间，用于补偿开始时间
        ServiceTimer timer = getById(id);
        if (timer == null) throw BusinessException.notFound("计时记录不存在");
        if (!Integer.valueOf(2).equals(timer.getStatus())) throw BusinessException.badRequest("只能继续已暂停的计时");
        if (timer.getPausedAt() == null) throw BusinessException.badRequest("暂停时间异常");
        long pausedMs = Duration.between(timer.getPausedAt(), LocalDateTime.now(ZONE)).toMillis();
        int rows = jdbcTemplate.update(
            "UPDATE service_timer SET status = 1, started_at = ?, paused_at = NULL WHERE id = ? AND status = 2",
            timer.getStartedAt().plusNanos(pausedMs * 1_000_000), id);
        if (rows == 0) throw BusinessException.badRequest("只能继续已暂停的计时");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, Integer actualDuration) {
        ServiceTimer timer = getById(id);
        if (timer == null) throw BusinessException.notFound("计时记录不存在");

        int duration = actualDuration != null ? actualDuration :
            (int) Duration.between(timer.getStartedAt(), LocalDateTime.now(ZONE)).toMinutes();

        // 原子更新计时器状态：只有进行中或暂停的才能完成
        int timerRows = jdbcTemplate.update(
            "UPDATE service_timer SET status = 3, completed_at = ?, actual_duration = ? " +
            "WHERE id = ? AND status IN (1, 2)",
            LocalDateTime.now(ZONE), duration, id);
        if (timerRows == 0) throw BusinessException.badRequest("计时已结束或状态不符");

        // 原子释放技师状态（允许静默失败：技师可能已被手动释放）
        if (timer.getEmployeeId() != null) {
            int techRows = jdbcTemplate.update(
                "UPDATE technician_status SET status = 'AVAILABLE', current_customer_name = NULL, " +
                "current_service_name = NULL, last_status_changed = ? " +
                "WHERE employee_id = ? AND status = 'BUSY'",
                LocalDateTime.now(ZONE), timer.getEmployeeId());
            if (techRows == 0) {
                log.warn("技师释放时状态非BUSY（可能已手动释放）: employeeId={}", String.valueOf(timer.getEmployeeId()));
            }
        }

        // 回写预约状态为已完成（允许静默失败：预约可能已被其他路径关闭）
        if (timer.getAppointmentId() != null) {
            int apptRows = jdbcTemplate.update(
                "UPDATE appointment SET status = 3 WHERE id = ? AND status IN (1, 2)",
                timer.getAppointmentId());
            if (apptRows == 0) {
                log.warn("预约状态回写失败（状态已变更）: appointmentId={}", String.valueOf(timer.getAppointmentId()));
            }
        }

        autoCreateOrder(timer);

        eventPublisher.publishEvent(new TimerCompletedEvent(
            timer.getId(), timer.getQueueId(), timer.getEmployeeId(),
            timer.getMemberId(), null));
    }

    private void autoCreateOrder(ServiceTimer timer) {
        ServiceItem item = null;
        if (timer.getServiceItemId() != null && timer.getServiceItemId() > 0) {
            item = serviceItemMapper.selectById(timer.getServiceItemId());
        }
        if (item == null) {
            item = serviceItemMapper.selectOne(
                new LambdaQueryWrapper<ServiceItem>().eq(ServiceItem::getName, timer.getServiceItemName()));
        }
        Long itemId = item != null ? item.getId() : null;
        BigDecimal itemPrice = item != null && item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;

        CreateOrderDTO.OrderItemDTO orderItem = new CreateOrderDTO.OrderItemDTO();
        orderItem.setItemId(itemId != null ? itemId : 0L);
        orderItem.setItemName(timer.getServiceItemName());
        orderItem.setItemPrice(itemPrice);
        orderItem.setQuantity(1);

        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setMemberId(timer.getMemberId());
        dto.setEmployeeId(timer.getEmployeeId());
        dto.setPayMethod(1);
        dto.setPayAmount(itemPrice);
        dto.setItems(List.of(orderItem));

        consumptionOrderService.createOrder(dto);
        log.info("服务计时完成自动创建订单: timerId={}, memberId={}, service={}, amount={}",
            timer.getId(), timer.getMemberId(), timer.getServiceItemName(), itemPrice);
    }

    @Override
    public List<ServiceTimerActiveVO> listActive() {
        List<ServiceTimer> timers = list(
            new LambdaQueryWrapper<ServiceTimer>().in(ServiceTimer::getStatus, 1, 2));
        List<ServiceTimerActiveVO> result = new ArrayList<>();
        for (ServiceTimer t : timers) {
            ServiceTimerActiveVO vo = new ServiceTimerActiveVO();
            vo.setId(t.getId());
            vo.setEmployeeId(t.getEmployeeId());
            vo.setMemberId(t.getMemberId());
            vo.setServiceItemId(t.getServiceItemId());
            vo.setServiceItemName(t.getServiceItemName());
            vo.setPlannedDuration(t.getPlannedDuration());
            vo.setStatus(t.getStatus());
            vo.setStartedAt(t.getStartedAt());
            vo.setPausedAt(t.getPausedAt());
            long elapsed = Duration.between(t.getStartedAt(), LocalDateTime.now(ZONE)).toSeconds();
            vo.setElapsedSeconds(Math.max(0, elapsed));
            vo.setRemainingSeconds(Math.max(0, t.getPlannedDuration() * 60L - elapsed));
            result.add(vo);
        }
        return result;
    }
}
