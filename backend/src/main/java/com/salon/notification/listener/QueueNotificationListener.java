package com.salon.notification.listener;

import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.notification.enums.NotificationType;
import com.salon.notification.service.SmsService;
import com.salon.queue.entity.ServiceQueue;
import com.salon.queue.service.ServiceQueueService;
import com.salon.timer.event.TimerCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueNotificationListener {

    private final SmsService smsService;
    private final ServiceQueueService serviceQueueService;
    private final MemberMapper memberMapper;

    @Async("notificationExecutor")
    @EventListener
    public void onTimerCompleted(TimerCompletedEvent event) {
        log.info("QueueNotificationListener: timer completed, checking next waiting. timerId={}, queueId={}",
            event.timerId(), event.queueId());

        try {
            ServiceQueue next = serviceQueueService.findNextWaitingToday();
            if (next == null) {
                log.info("QueueNotificationListener: no waiting customers, skip notification");
                return;
            }

            if (next.getMemberId() != null) {
                Member member = memberMapper.selectById(next.getMemberId());
                if (member != null && member.getPhone() != null && !member.getPhone().isEmpty()) {
                    String content = "尊敬的 " + (member.getName() != null ? member.getName() : "顾客") +
                        "，您的排队号 " + next.getQueueNumber() + " 即将轮到您，请做好准备。";
                    smsService.send(member.getPhone(), NotificationType.QUEUE_NOTIFY.getType(),
                        NotificationType.QUEUE_NOTIFY.getTemplateCode(), content, next.getMemberId());
                    log.info("QueueNotificationListener: sent notification to memberId={}, queueNumber={}",
                        next.getMemberId(), next.getQueueNumber());
                }
            }
        } catch (Exception e) {
            log.error("QueueNotificationListener: failed to process notification for timerId={}",
                event.timerId(), e);
        }
    }
}
