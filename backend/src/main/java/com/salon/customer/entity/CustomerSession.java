package com.salon.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customer_session")
public class CustomerSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String token;
    private String loginMethod;
    private LocalDateTime expireAt;
    private LocalDateTime createTime;
}
