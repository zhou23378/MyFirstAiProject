package com.salon.notification.vo;

import com.salon.notification.entity.NotificationLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "通知日志视图")
public class NotificationLogVO {

    public static NotificationLogVO from(NotificationLog entity) {
        if (entity == null) return null;
        NotificationLogVO vo = new NotificationLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "通知类型")
    private String type;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "状态(0=发送中/1=成功/2=失败)")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "发送时间")
    private LocalDateTime sentAt;

    @Schema(description = "是否已读(0=未读 1=已读)")
    private Integer isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
