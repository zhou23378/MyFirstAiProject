package com.salon.schedule.controller;

import com.salon.common.result.Result;
import com.salon.schedule.dto.ScheduleBatchDTO;
import com.salon.schedule.entity.Schedule;
import com.salon.schedule.service.ScheduleService;
import com.salon.schedule.vo.ScheduleWeekVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "排班管理", description = "员工排班接口")
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/week")
    @Operation(summary = "周排班视图")
    public Result<List<ScheduleWeekVO>> week(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(scheduleService.weekView(startDate, endDate));
    }

    @PostMapping("/batch")
    @Operation(summary = "批量设置某员工排班")
    public Result<Void> batchSet(@RequestParam Long employeeId, @Valid @RequestBody List<ScheduleBatchDTO> list) {
        List<Schedule> schedules = new java.util.ArrayList<>();
        for (ScheduleBatchDTO dto : list) {
            Schedule s = new Schedule();
            s.setDate(dto.getDate());
            s.setShiftId(dto.getShiftId());
            schedules.add(s);
        }
        scheduleService.batchSet(employeeId, schedules);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除排班")
    public Result<Void> delete(@PathVariable Long id) {
        scheduleService.removeById(id);
        return Result.success();
    }
}
