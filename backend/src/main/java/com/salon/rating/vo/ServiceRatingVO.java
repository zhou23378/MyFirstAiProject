package com.salon.rating.vo;

import com.salon.rating.entity.ServiceRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "服务评价视图")
public class ServiceRatingVO {

    public static ServiceRatingVO from(ServiceRating entity) {
        if (entity == null) return null;
        ServiceRatingVO vo = new ServiceRatingVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "消费订单ID")
    private Long orderId;

    @Schema(description = "评分 1-5")
    private Integer rating;

    @Schema(description = "评价标签 JSON")
    private String tags;

    @Schema(description = "评价内容")
    private String comment;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
