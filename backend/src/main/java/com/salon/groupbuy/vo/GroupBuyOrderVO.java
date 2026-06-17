package com.salon.groupbuy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupBuyOrderVO {
    private Long id;
    private Long templateId;
    private String orderNo;
    private Long leaderId;
    private String leaderName;
    private String leaderPhone;
    private BigDecimal groupPrice;
    private BigDecimal originalPrice;
    private Integer groupSize;
    private Integer currentSize;
    private LocalDateTime expireTime;
    private Integer status;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private List<GroupBuyParticipantVO> participants;
}
