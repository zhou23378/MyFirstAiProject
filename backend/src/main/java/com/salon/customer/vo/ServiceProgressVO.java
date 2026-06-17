package com.salon.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "顾客服务进度")
public class ServiceProgressVO {

    @Schema(description = "状态: IN_SERVICE(服务中) / WAITING(排队中) / IDLE(空闲)")
    private String status;

    // 排队中
    @Schema(description = "排队号")
    private Integer queueNumber;

    @Schema(description = "前面等待人数")
    private Integer waitingAhead;

    // 服务中
    @Schema(description = "技师姓名")
    private String technicianName;

    @Schema(description = "服务项目名")
    private String serviceName;

    @Schema(description = "已过秒数")
    private Long elapsedSeconds;

    @Schema(description = "剩余秒数")
    private Long remainingSeconds;

    @Schema(description = "计划时长（分钟）")
    private Integer plannedDuration;

    @Schema(description = "计时状态: RUNNING(进行中) / PAUSED(已暂停)")
    private String timerStatus;

    public static ServiceProgressVO idle() {
        ServiceProgressVO vo = new ServiceProgressVO();
        vo.setStatus("IDLE");
        return vo;
    }

    public static ServiceProgressVO waiting(int queueNumber, int waitingAhead) {
        ServiceProgressVO vo = new ServiceProgressVO();
        vo.setStatus("WAITING");
        vo.setQueueNumber(queueNumber);
        vo.setWaitingAhead(waitingAhead);
        return vo;
    }

    public static ServiceProgressVO inService(String technicianName, String serviceName,
                                               long elapsedSeconds, long remainingSeconds,
                                               int plannedDuration, boolean paused) {
        ServiceProgressVO vo = new ServiceProgressVO();
        vo.setStatus("IN_SERVICE");
        vo.setTechnicianName(technicianName);
        vo.setServiceName(serviceName);
        vo.setElapsedSeconds(elapsedSeconds);
        vo.setRemainingSeconds(remainingSeconds);
        vo.setPlannedDuration(plannedDuration);
        vo.setTimerStatus(paused ? "PAUSED" : "RUNNING");
        return vo;
    }
}
