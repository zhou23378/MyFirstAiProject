package com.salon.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员查询请求 DTO
 * <p>
 * 封装会员列表查询的筛选条件和分页参数
 * </p>
 */
@Data
@Schema(description = "会员查询请求")
public class MemberQueryDTO {

    /** 会员姓名（模糊查询） */
    @Schema(description = "会员姓名（模糊查询）", example = "张")
    private String name;

    /** 手机号（模糊查询） */
    @Schema(description = "手机号（模糊查询）", example = "138")
    private String phone;

    /** 余额下限 */
    @Schema(description = "余额下限", example = "1000")
    private BigDecimal balanceMin;

    /** 余额上限 */
    @Schema(description = "余额上限", example = "5000")
    private BigDecimal balanceMax;

    /** 等级ID精确匹配 */
    @Schema(description = "等级ID", example = "3")
    private Integer level;

    /** 性别：1=男 2=女 0=未知 */
    @Schema(description = "性别：1=男 2=女 0=未知", example = "1")
    private Integer gender;

    /** 累计消费金额下限 */
    @Schema(description = "累计消费金额下限", example = "500")
    private BigDecimal totalConsumeMin;

    /** 累计消费金额上限 */
    @Schema(description = "累计消费金额上限", example = "5000")
    private BigDecimal totalConsumeMax;

    /** 最后消费距今天数下限（>N 天未消费） */
    @Schema(description = "最后消费距今天数下限", example = "30")
    private Integer lastConsumeDays;

    /** 当前页码，默认 1 */
    @Schema(description = "当前页码", example = "1")
    private Integer page = 1;

    /** 每页条数，默认 10 */
    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;
}
