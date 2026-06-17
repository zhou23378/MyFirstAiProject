package com.salon.dashboard.statcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dashboard_stat_card")
public class DashboardStatCard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer slot;
    private String statKey;
    private String label;
    private String path;
}
