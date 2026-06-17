package com.salon.timer.event;

public record TimerCompletedEvent(Long timerId, Long queueId, Long employeeId, Long memberId, String memberName) {
}
