package com.salon.notification.scheduler;

import com.salon.notification.enums.NotificationType;
import com.salon.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 预约前 1 小时精准提醒。
 * 每 15 分钟扫描即将开始的预约，发送 SMS 后标记 reminded=1 防重复。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final SmsService smsService;

    /** 每 15 分钟执行一次 */
    @Scheduled(cron = "0 */15 * * * *")
    public void processAppointmentReminders() {
        Boolean locked = jdbcTemplate.queryForObject(
            "SELECT GET_LOCK('appointment_reminder_scheduler', 0)", Boolean.class);
        if (!Boolean.TRUE.equals(locked)) {
            log.info("AppointmentReminderScheduler: lock not acquired, skip");
            return;
        }
        try {
            doProcess();
        } finally {
            jdbcTemplate.execute("SELECT RELEASE_LOCK('appointment_reminder_scheduler')");
        }
    }

    private void doProcess() {
        LocalDate today = LocalDate.now();
        // 查找 45-75 分钟后开始的预约（1小时±15分钟窗口，配合每15分钟调度）
        LocalTime windowStart = LocalTime.now().plusMinutes(45);
        LocalTime windowEnd = LocalTime.now().plusMinutes(75);

        List<Map<String, Object>> appointments = jdbcTemplate.queryForList(
            "SELECT a.id, a.member_id, a.start_time, a.employee_id, " +
            "m.name AS member_name, m.phone AS member_phone, si.name AS service_name " +
            "FROM appointment a " +
            "JOIN member m ON a.member_id = m.id " +
            "JOIN service_item si ON a.service_item_id = si.id " +
            "WHERE a.appointment_date = ? AND a.status = 1 AND a.reminded = 0 " +
            "AND a.start_time >= ? AND a.start_time <= ?",
            today, windowStart, windowEnd);

        log.info("AppointmentReminderScheduler: found {} appointments in 1h window", appointments.size());

        for (Map<String, Object> row : appointments) {
            Long appointmentId = (Long) row.get("id");
            try {
                Long memberId = (Long) row.get("member_id");
                String memberName = (String) row.get("member_name");
                String phone = (String) row.get("member_phone");
                String serviceName = (String) row.get("service_name");
                LocalTime startTime = (LocalTime) row.get("start_time");

                if (phone == null || phone.isEmpty()) continue;

                String content = "亲爱的" + memberName + "，您预约的" + today +
                    " " + startTime.toString().substring(0, 5) +
                    " " + serviceName + "服务即将在1小时后开始，请准时到店。";

                smsService.send(phone, NotificationType.APPOINTMENT_REMINDER.getType(),
                    NotificationType.APPOINTMENT_REMINDER.getTemplateCode(), content, memberId);

                // 标记已提醒
                jdbcTemplate.update("UPDATE appointment SET reminded = 1 WHERE id = ?", appointmentId);
                log.info("Appointment reminder sent: memberId={}, appointmentId={}, time={}",
                    memberId, appointmentId, startTime);
            } catch (Exception e) {
                log.error("AppointmentReminderScheduler: error for appointment {}", appointmentId, e);
            }
        }
    }
}
