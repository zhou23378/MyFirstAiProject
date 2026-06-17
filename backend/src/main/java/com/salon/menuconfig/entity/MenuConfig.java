package com.salon.menuconfig.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("menu_config")
public class MenuConfig {
    private Long id;
    private String menuIndex;
    private String groupName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
