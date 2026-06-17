package com.salon.notification.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.notification.entity.NotificationLog;
import com.salon.notification.mapper.NotificationLogMapper;
import com.salon.notification.vo.NotificationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "通知中心", description = "通知列表、未读计数、标记已读")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLogMapper notificationLogMapper;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    @Operation(summary = "分页查询通知列表")
    public Result<PageResult<NotificationLogVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<NotificationLog> p = notificationLogMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<NotificationLog>()
                        .orderByDesc(NotificationLog::getCreateTime)
        );
        return Result.success(PageResult.of(p, NotificationLogVO::from));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    public Result<Long> unreadCount() {
        Long count = notificationLogMapper.selectCount(
                new LambdaQueryWrapper<NotificationLog>()
                        .eq(NotificationLog::getIsRead, 0)
        );
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记单条通知为已读")
    public Result<Void> markRead(@PathVariable Long id) {
        jdbcTemplate.update(
                "UPDATE notification_log SET is_read = 1 WHERE id = ? AND is_read = 0", id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记为已读")
    public Result<Void> markAllRead() {
        jdbcTemplate.update(
                "UPDATE notification_log SET is_read = 1 WHERE is_read = 0");
        return Result.success();
    }
}
