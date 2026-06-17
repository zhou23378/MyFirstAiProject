package com.salon.dashboard.quickaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dashboard_quick_action")
public class DashboardQuickAction {
    private Long id;
    private Integer slot;
    private String label;
    private String path;
    private LocalDateTime updateTime;
}
