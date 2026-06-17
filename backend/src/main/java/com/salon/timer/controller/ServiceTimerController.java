package com.salon.timer.controller;

import com.salon.common.result.Result;
import com.salon.timer.dto.TimerCompleteDTO;
import com.salon.timer.dto.TimerStartDTO;
import com.salon.timer.entity.ServiceTimer;
import com.salon.timer.service.ServiceTimerService;
import com.salon.timer.vo.ServiceTimerActiveVO;
import com.salon.timer.vo.ServiceTimerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "服务计时", description = "服务计时管理")
@RestController
@RequestMapping("/api/service-timer")
@RequiredArgsConstructor
public class ServiceTimerController {

    private final ServiceTimerService serviceTimerService;

    @PostMapping("/start")
    @Operation(summary = "开始计时")
    public Result<ServiceTimerVO> start(@Valid @RequestBody TimerStartDTO dto) {
        ServiceTimer entity = serviceTimerService.start(
            dto.getAppointmentId(), dto.getQueueId(), dto.getEmployeeId(), dto.getMemberId(),
            dto.getServiceItemId(), dto.getServiceItemName(),
            dto.getPlannedDuration() != null ? dto.getPlannedDuration() : 30);
        return Result.success(ServiceTimerVO.from(entity));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "暂停计时")
    public Result<Void> pause(@PathVariable Long id) {
        serviceTimerService.pause(id);
        return Result.success();
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "继续计时")
    public Result<Void> resume(@PathVariable Long id) {
        serviceTimerService.resume(id);
        return Result.success();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成计时")
    public Result<Void> complete(@PathVariable Long id, @Valid @RequestBody(required = false) TimerCompleteDTO dto) {
        Integer actualDuration = dto != null ? dto.getActualDuration() : null;
        serviceTimerService.complete(id, actualDuration);
        return Result.success();
    }

    @GetMapping("/active")
    @Operation(summary = "当前进行中的计时列表")
    public Result<List<ServiceTimerActiveVO>> active() {
        return Result.success(serviceTimerService.listActive());
    }
}
