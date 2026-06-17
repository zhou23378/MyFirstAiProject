package com.salon.schedule.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.schedule.dto.ClockDTO;
import com.salon.schedule.entity.Attendance;
import com.salon.schedule.service.AttendanceService;
import com.salon.schedule.vo.AttendancePageVO;
import com.salon.schedule.vo.AttendanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "考勤管理", description = "打卡与考勤记录")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/page")
    @Operation(summary = "分页查询考勤记录")
    public Result<PageResult<AttendancePageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long employeeId) {
        Page<AttendancePageVO> p = attendanceService.pageWithNames(page, pageSize, date, employeeId);
        return Result.success(PageResult.of(p));
    }

    @PostMapping("/clock")
    @Operation(summary = "上班/下班打卡（type: 1=上班 2=下班）")
    public Result<AttendanceVO> clock(@Valid @RequestBody ClockDTO dto) {
        Attendance entity = attendanceService.clock(dto.getEmployeeId(), dto.getType());
        return Result.success(AttendanceVO.from(entity));
    }
}
