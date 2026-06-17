package com.salon.queue.controller;

import com.salon.common.result.Result;
import com.salon.queue.dto.AssignReqDTO;
import com.salon.queue.dto.EnqueueReqDTO;
import com.salon.queue.entity.ServiceQueue;
import com.salon.queue.service.ServiceQueueService;
import com.salon.queue.vo.QueueTodayStatsVO;
import com.salon.queue.vo.ServiceQueueItemVO;
import com.salon.queue.vo.ServiceQueueVO;
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

@Tag(name = "轮牌排队", description = "服务轮牌排队管理")
@RestController
@RequestMapping("/api/service-queue")
@RequiredArgsConstructor
public class ServiceQueueController {

    private final ServiceQueueService serviceQueueService;

    @GetMapping("/list")
    @Operation(summary = "今日排队列表")
    public Result<List<ServiceQueueItemVO>> list() {
        return Result.success(serviceQueueService.listToday());
    }

    @PostMapping("/enqueue")
    @Operation(summary = "会员入队")
    public Result<ServiceQueueVO> enqueue(@Valid @RequestBody EnqueueReqDTO dto) {
        ServiceQueue entity = serviceQueueService.enqueue(
            dto.getMemberId(), dto.getMemberName(), dto.getServiceItemId(), dto.getServiceItemName());
        return Result.success(ServiceQueueVO.from(entity));
    }

    @PostMapping("/assign")
    @Operation(summary = "分配技师")
    public Result<Void> assign(@Valid @RequestBody AssignReqDTO dto) {
        serviceQueueService.assign(dto.getQueueId(), dto.getEmployeeId());
        return Result.success();
    }

    @PostMapping("/{id}/skip")
    @Operation(summary = "跳过")
    public Result<Void> skip(@PathVariable Long id) {
        serviceQueueService.skip(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消排队")
    public Result<Void> cancel(@PathVariable Long id) {
        serviceQueueService.cancel(id);
        return Result.success();
    }

    @GetMapping("/today-stats")
    @Operation(summary = "今日排队统计")
    public Result<QueueTodayStatsVO> todayStats() {
        return Result.success(serviceQueueService.todayStats());
    }
}
